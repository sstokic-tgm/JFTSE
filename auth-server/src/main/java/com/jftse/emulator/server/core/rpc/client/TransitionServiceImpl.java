package com.jftse.emulator.server.core.rpc.client;

import com.google.common.util.concurrent.ListenableFuture;
import com.jftse.emulator.server.core.utils.FutureUtils;
import com.jftse.entities.database.model.ServerType;
import com.jftse.proto.auth.TransitionRequest;
import com.jftse.proto.auth.TransitionResponse;
import com.jftse.proto.auth.TransitionServiceGrpc;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class TransitionServiceImpl {

    @GrpcClient("game-server")
    private TransitionServiceGrpc.TransitionServiceFutureStub gameServerStub;

    @GrpcClient("chat-server")
    private TransitionServiceGrpc.TransitionServiceFutureStub chatServerStub;


    public CompletableFuture<TransitionResponse> notifyTransition(ServerType serverType, Long accountId) {
        TransitionServiceGrpc.TransitionServiceFutureStub selectedStub = (serverType == ServerType.GAME_SERVER)
                ? gameServerStub : chatServerStub;

        TransitionRequest request = TransitionRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        ListenableFuture<TransitionResponse> response = selectedStub.notifyTransition(request);
        return FutureUtils.toCompletableFuture(response);
    }
}
