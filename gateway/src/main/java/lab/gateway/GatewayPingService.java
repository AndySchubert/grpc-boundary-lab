package lab.gateway;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lab.grpc.PingRequest;
import lab.grpc.PingResponse;
import lab.grpc.PingServiceGrpc;

public final class GatewayPingService extends PingServiceGrpc.PingServiceImplBase {
    private final PingServiceGrpc.PingServiceStub backendStub;

    GatewayPingService(PingServiceGrpc.PingServiceStub backendStub) {
        this.backendStub = backendStub;
    }

    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        backendStub.ping(request, new StreamObserver<PingResponse>() {
            @Override
            public void onNext(PingResponse value) {
                responseObserver.onNext(value);
            }

            @Override
            public void onError(Throwable t) {
                responseObserver.onError(Status.fromThrowable(t)
                        .withDescription("backend call failed")
                        .asRuntimeException());
            }

            @Override
            public void onCompleted() {
                responseObserver.onCompleted();
            }
        });
    }
}
