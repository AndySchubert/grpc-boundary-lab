package lab.loadgen;

import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import lab.grpc.PingRequest;
import lab.grpc.PingServiceGrpc;

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

        long t0 = System.nanoTime();
        for (int i = 0; i < n; i++) {
            var resp = stub.ping(PingRequest.newBuilder().setMessage("hi " + i).build());
            if (i == 0) {
                System.out.println("example response: " + resp.getMessage());
            }
        }
        long t1 = System.nanoTime();

        double seconds = (t1 - t0) / 1_000_000_000.0;
        System.out.printf("sent %d requests in %.3fs (%.2f req/s)%n", n, seconds, n / seconds);

        ch.shutdown().awaitTermination(3, TimeUnit.SECONDS);
    }
}
