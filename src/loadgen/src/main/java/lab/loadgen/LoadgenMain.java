package lab.loadgen;

import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;

import org.HdrHistogram.ConcurrentHistogram;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lab.grpc.PingRequest;
import lab.grpc.PingServiceGrpc;

public final class LoadgenMain {

    private static void runPhase(
            ExecutorService pool,
            ManagedChannel ch,
            int n,
            int c,
            boolean record,
            boolean printExample,
            ConcurrentHistogram hist,
            LongAdder ok,
            LongAdder errors,
            long deadlineMs
    ) throws InterruptedException {

        CountDownLatch latch = new CountDownLatch(c);
        AtomicInteger printed = new AtomicInteger(0);

        for (int worker = 0; worker < c; worker++) {
            final int workerId = worker;

            pool.submit(() -> {
                try {
                    var localStub = PingServiceGrpc.newBlockingStub(ch)
                            .withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);

                    int base = n / c;
                    int extra = n % c;
                    int myN = base + (workerId < extra ? 1 : 0);

                    int startIndex = workerId * base + Math.min(workerId, extra);

                    for (int j = 0; j < myN; j++) {
                        int i = startIndex + j;
                        long startNs = System.nanoTime();

                        try {
                            var resp = localStub.ping(PingRequest.newBuilder().setMessage("hi " + i).build());
                            ok.increment();

                            if (record) {
                                long durUs = TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - startNs);
                                if (durUs < 0) durUs = 0;
                                hist.recordValue(durUs);

                                if (printExample && workerId == 0 && j == 0) {
                                    System.out.println("example response: " + resp.getMessage());
                                }
                            }
                        } catch (Exception e) {
                            errors.increment();
                            int p = printed.getAndIncrement();
                            if (p < 3) {
                                System.out.println("error example: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                            }
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
    }

    public static void main(String[] args) throws Exception {
        String host = System.getenv().getOrDefault("TARGET_HOST", "127.0.0.1");
        int port = Integer.parseInt(System.getenv().getOrDefault("TARGET_PORT", "50052"));
        int n = Integer.parseInt(System.getenv().getOrDefault("REQUESTS", "100"));
        int c = Integer.parseInt(System.getenv().getOrDefault("CONCURRENCY", "1"));
        int warmup = Integer.parseInt(System.getenv().getOrDefault("WARMUP", "2000"));
        long deadlineMs = Long.parseLong(System.getenv().getOrDefault("DEADLINE_MS", "20000"));
        int runs = Integer.parseInt(System.getenv().getOrDefault("RUNS", "1"));

        ExecutorService pool = Executors.newFixedThreadPool(c);

        ManagedChannel ch = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        try {
            // Warmup (no recording, ignore counters)
            if (warmup > 0) {
                System.out.printf("warmup: %d requests with concurrency=%d%n", warmup, c);
                runPhase(pool, ch, warmup, c, false, false, null,
                        new LongAdder(), new LongAdder(), deadlineMs);
            }

            System.out.println("run,attempted,ok,errors,concurrency,seconds,ok_rps,p50_us,p95_us,p99_us,max_us");

            double minRps = Double.POSITIVE_INFINITY;
            double maxRps = 0.0;
            double sumRps = 0.0;

            for (int r = 1; r <= runs; r++) {
                ConcurrentHistogram hist = new ConcurrentHistogram(60_000_000L, 3); // micros
                LongAdder ok = new LongAdder();
                LongAdder errors = new LongAdder();

                long t0 = System.nanoTime();
                runPhase(pool, ch, n, c, true, r == 1, hist, ok, errors, deadlineMs);
                long t1 = System.nanoTime();

                double seconds = (t1 - t0) / 1_000_000_000.0;
                long okCount = ok.sum();
                long errCount = errors.sum();
                double okRps = okCount / seconds;

                long p50 = hist.getValueAtPercentile(50.0);
                long p95 = hist.getValueAtPercentile(95.0);
                long p99 = hist.getValueAtPercentile(99.0);
                long max = hist.getMaxValue();

                System.out.printf(Locale.ROOT,
                        "%d,%d,%d,%d,%d,%.3f,%.2f,%d,%d,%d,%d%n",
                        r, n, okCount, errCount, c, seconds, okRps, p50, p95, p99, max);

                minRps = Math.min(minRps, okRps);
                maxRps = Math.max(maxRps, okRps);
                sumRps += okRps;
            }

            if (runs > 1) {
                System.out.printf(Locale.ROOT,
                        "ok_rps summary: avg=%.2f min=%.2f max=%.2f%n",
                        (sumRps / runs), minRps, maxRps);
            }
        } finally {
            pool.shutdown();
            pool.awaitTermination(30, TimeUnit.SECONDS);

            ch.shutdown().awaitTermination(3, TimeUnit.SECONDS);
        }
    }
}
