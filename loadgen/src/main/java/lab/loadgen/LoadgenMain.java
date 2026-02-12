package lab.loadgen;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lab.grpc.PingRequest;
import lab.grpc.PingServiceGrpc;

import org.HdrHistogram.ConcurrentHistogram;

import java.util.concurrent.TimeUnit;

public final class LoadgenMain {
    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("BACKEND_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50052"));
        int n = Integer.parseInt(System.getenv().getOrDefault("REQUESTS", "100"));

        ManagedChannel ch = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        var stub = PingServiceGrpc.newBlockingStub(ch);

        ConcurrentHistogram hist = new ConcurrentHistogram(60_000_000L, 3); // up to 60s in microseconds, 3 sig digits

        long t0 = System.nanoTime();
        
        for (int i = 0; i < n; i++) {
            long startNs = System.nanoTime();
            var resp = stub.ping(PingRequest.newBuilder().setMessage("hi " + i).build());
            long durUs = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNs);
            if (durUs < 0) durUs = 0;
            hist.recordValue(durUs);

            if (i == 0) {
                System.out.println("example response: " + resp.getMessage());
            }
        }

        long t1 = System.nanoTime();
        System.out.printf("latency(us): p50=%d p95=%d p99=%d max=%d%n",
                hist.getValueAtPercentile(50.0),
                hist.getValueAtPercentile(95.0),
                hist.getValueAtPercentile(99.0),
                hist.getMaxValue()
        );
        
        double seconds = (t1 - t0) / 1_000_000_000.0;
        System.out.printf("sent %d requests in %.3fs (%.2f req/s)%n", n, seconds, n / seconds);

        ch.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }
}
