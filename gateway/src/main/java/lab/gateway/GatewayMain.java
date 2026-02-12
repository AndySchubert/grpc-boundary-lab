package lab.gateway;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import lab.grpc.PingServiceGrpc;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class GatewayMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        GatewayConfig config = GatewayConfig.fromEnv();

        NettyChannelBuilder channelBuilder = NettyChannelBuilder
                .forAddress(config.backendHost, config.backendPort)
                .usePlaintext();

        if (config.clientThreads > 0) {
            System.out.println("Using fixed thread pool for gateway client: " + config.clientThreads);
            channelBuilder.executor(Executors.newFixedThreadPool(config.clientThreads));
        }

        ManagedChannel backendChannel = channelBuilder.build();
        var backendStub = PingServiceGrpc.newStub(backendChannel);

        NettyServerBuilder serverBuilder = NettyServerBuilder.forPort(config.port)
                .addService(new GatewayPingService(backendStub));

        if (config.serverThreads > 0) {
            System.out.println("Using fixed thread pool for gateway server: " + config.serverThreads);
            serverBuilder.executor(Executors.newFixedThreadPool(config.serverThreads));
        }

        Server server = serverBuilder.build().start();

        System.out.println("gateway listening on :" + config.port + " (forwarding to " + config.backendHost + ":" + config.backendPort + ")");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("shutting down gateway...");
            try {
                server.shutdown().awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException ignored) {
            }
            backendChannel.shutdown();
        }));

        server.awaitTermination();
    }
}

