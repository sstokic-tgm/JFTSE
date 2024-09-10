package com.jftse.emulator.server.core.rpc.server;

import com.jftse.proto.auth.TransitionRequest;
import com.jftse.proto.auth.TransitionResponse;
import com.jftse.proto.auth.TransitionServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.ConcurrentHashMap;

@GrpcService
@Log4j2
public class TransitionServerServiceImpl extends TransitionServiceGrpc.TransitionServiceImplBase {
    private final ConcurrentHashMap<Long, Boolean> transitionMap = new ConcurrentHashMap<>();

    @Override
    public void notifyTransition(TransitionRequest request, StreamObserver<TransitionResponse> responseObserver) {
        boolean success = transitionMap.getOrDefault(request.getAccountId(), false);
        TransitionResponse response = TransitionResponse.newBuilder()
                .setSuccess(success)
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();

        transitionMap.remove(request.getAccountId());
    }

    public void markAccountAsLoggedIn(Long accountId) {
        transitionMap.put(accountId, true);
    }
}
