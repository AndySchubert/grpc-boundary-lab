package lab.backend;

import io.grpc.stub.StreamObserver;
import lab.grpc.PingRequest;
import lab.grpc.PingResponse;
import lab.grpc.PingServiceGrpc;

public final class PingServiceImpl extends PingServiceGrpc.PingServiceImplBase {
    @Override
    public void ping(PingRequest request, StreamObserver<PingResponse> responseObserver) {
        String msg = request.getMessage();
        PingResponse resp = PingResponse.newBuilder().setMessage("pong: " + msg).build();
        responseObserver.onNext(resp);
        responseObserver.onCompleted();
    }
}
