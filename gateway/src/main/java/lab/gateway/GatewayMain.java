package lab.gateway;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.Status;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import lab.grpc.PingRequest;
import lab.grpc.PingResponse;
import lab.grpc.PingServiceGrpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class GatewayMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(System.getenv().getOrDefault("GATEWAY_PORT", "50052"));
        String backendHost = System.getenv().getOrDefault("BACKEND_HOST", "127.0.0.1");
        int backendPort = Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50051"));

        ManagedChannel backendChannel = NettyChannelBuilder
                .forAddress(backendHost, backendPort)
                .usePlaintext()
                .build();

        var backendStub = PingServiceGrpc.newBlockingStub(backendChannel);

        Server server = NettyServerBuilder.forPort(port)
                .addService(new GatewayPingService(backendStub))
                .build()
                .start();

        System.out.println("gateway listening on :" + port + " (forwarding to " + backendHost + ":" + backendPort + ")");

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

    static final class GatewayPingService extends PingServiceGrpc.PingServiceImplBase {
        private final PingServiceGrpc.PingServiceBlockingStub backend;

        GatewayPingService(PingServiceGrpc.PingServiceBlockingStub backend) {
            this.backend = backend;
        }

        @Override
        public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
            try {
                PingResponse resp = backend.ping(request);
                responseObserver.onNext(resp);
                responseObserver.onCompleted();
            } catch (Exception e) {
                responseObserver.onError(
                        Status.UNAVAILABLE
                                .withDescription("backend call failed: " + e.getMessage())
                                .withCause(e)
                                .asRuntimeException()
                );
            }
        }
    }
}
