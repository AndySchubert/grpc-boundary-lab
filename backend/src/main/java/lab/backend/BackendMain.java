package lab.backend;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class BackendMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50051"));
        int threads = Integer.parseInt(System.getenv().getOrDefault("BACKEND_THREADS", "0"));

        NettyServerBuilder builder = NettyServerBuilder.forPort(port)
                .addService(new PingServiceImpl());

        if (threads > 0) {
            System.out.println("Using fixed thread pool for backend: " + threads);
            builder.executor(Executors.newFixedThreadPool(threads));
        }

        Server server = builder.build().start();

        System.out.println("backend listening on :" + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("shutting down backend...");
            try {
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
        }));

        server.awaitTermination();
    }
}

