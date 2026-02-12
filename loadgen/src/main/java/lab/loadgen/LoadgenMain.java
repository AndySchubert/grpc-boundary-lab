package lab.loadgen;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lab.grpc.PingRequest;
import lab.grpc.PingServiceGrpc;

import org.HdrHistogram.ConcurrentHistogram;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class LoadgenMain {
    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("BACKEND_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50052"));
        int n = Integer.parseInt(System.getenv().getOrDefault("REQUESTS", "100"));
        int c = Integer.parseInt(System.getenv().getOrDefault("CONCURRENCY", "1"));

        ManagedChannel ch = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        ConcurrentHistogram hist = new ConcurrentHistogram(60_000_000L, 3); // micros

        ExecutorService pool = Executors.newFixedThreadPool(c);
        CountDownLatch latch = new CountDownLatch(c);

        long t0 = System.nanoTime();

        for (int worker = 0; worker < c; worker++) {
            final int workerId = worker;

            pool.submit(() -> {
                try {
                    var localStub = PingServiceGrpc.newBlockingStub(ch);

                    int base = n / c;
                    int extra = n % c;
                    int myN = base + (workerId < extra ? 1 : 0);

                    int startIndex = workerId * base + Math.min(workerId, extra);

                    for (int j = 0; j < myN; j++) {
                        int i = startIndex + j;

                        long startNs = System.nanoTime();
                        var resp = localStub.ping(PingRequest.newBuilder().setMessage("hi " + i).build());
                        long durUs = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNs);
                        if (durUs < 0) durUs = 0;
                        hist.recordValue(durUs);

                        if (workerId == 0 && j == 0) {
                            System.out.println("example response: " + resp.getMessage());
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        long t1 = System.nanoTime();

        pool.shutdown();

        double seconds = (t1 - t0) / 1_000_000_000.0;
        System.out.printf("sent %d requests with concurrency=%d in %.3fs (%.2f req/s)%n",
                n, c, seconds, n / seconds);

        System.out.printf("latency(us): p50=%d p95=%d p99=%d max=%d%n",
                hist.getValueAtPercentile(50.0),
                hist.getValueAtPercentile(95.0),
                hist.getValueAtPercentile(99.0),
                hist.getMaxValue()
        );

        ch.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }
}
