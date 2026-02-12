package lab.gateway;

import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.StreamObserver;
import io.grpc.testing.GrpcCleanupRule;
import lab.grpc.PingRequest;
import lab.grpc.PingResponse;
import lab.grpc.PingServiceGrpc;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class IntegrationTest {

    private Server backendServer;
    private Server gatewayServer;
    private ManagedChannel channel;

    @BeforeEach
    public void setUp() throws IOException {
        String backendName = InProcessServerBuilder.generateName();
        String gatewayName = InProcessServerBuilder.generateName();

        // Start mock backend
        backendServer = InProcessServerBuilder
                .forName(backendName)
                .directExecutor()
                .addService(new PingServiceGrpc.PingServiceImplBase() {
                    @Override
                    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
                        responseObserver.onNext(PingResponse.newBuilder()
                                .setMessage("mock-pong: " + request.getMessage())
                                .build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();

        // Start gateway with in-process channel to backend
        ManagedChannel backendChannel = InProcessChannelBuilder.forName(backendName).directExecutor().build();
        PingServiceGrpc.PingServiceStub backendStub = PingServiceGrpc.newStub(backendChannel);

        gatewayServer = InProcessServerBuilder
                .forName(gatewayName)
                .directExecutor()
                .addService(new GatewayPingService(backendStub))
                .build()
                .start();

        channel = InProcessChannelBuilder.forName(gatewayName).directExecutor().build();
    }

    @AfterEach
    public void tearDown() {
        channel.shutdownNow();
        gatewayServer.shutdownNow();
        backendServer.shutdownNow();
    }

    @Test
    public void testGatewayForwarding() {
        PingServiceGrpc.PingServiceBlockingStub blockingStub = PingServiceGrpc.newBlockingStub(channel);
        PingResponse response = blockingStub.ping(PingRequest.newBuilder().setMessage("hello").build());
        assertEquals("mock-pong: hello", response.getMessage());
    }
}
