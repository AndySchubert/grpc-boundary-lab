package lab.backend;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import lab.grpc.PingRequest;
import lab.grpc.PingResponse;
import lab.grpc.PingServiceGrpc;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public final class BackendMain {
    public static void main(String[] args) throws IOException, InterruptedException {
        int port = Integer.parseInt(System.getenv().getOrDefault("BACKEND_PORT", "50051"));

        Server server = NettyServerBuilder.forPort(port)
                .addService(new PingServiceImpl())
                .build()
                .start();

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

    static final class PingServiceImpl extends PingServiceGrpc.PingServiceImplBase {
        @Override
        public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
            String msg = request.getMessage();
            PingResponse resp = PingResponse.newBuilder().setMessage("pong: " + msg).build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        }
    }
}
