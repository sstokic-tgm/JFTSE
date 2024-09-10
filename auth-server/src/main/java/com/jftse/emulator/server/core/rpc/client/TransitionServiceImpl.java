package com.jftse.emulator.server.core.rpc.client;

import com.jftse.entities.database.model.ServerType;
import com.jftse.proto.auth.TransitionRequest;
import com.jftse.proto.auth.TransitionResponse;
import com.jftse.proto.auth.TransitionServiceGrpc;
import com.jftse.server.core.proto.interfaces.TransitionCallback;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class TransitionServiceImpl {

    @GrpcClient("game-server")
    private TransitionServiceGrpc.TransitionServiceStub gameServerStub;

    @GrpcClient("chat-server")
    private TransitionServiceGrpc.TransitionServiceStub chatServerStub;


    public void notifyTransition(ServerType serverType, Long accountId, TransitionCallback callback) {
        TransitionServiceGrpc.TransitionServiceStub selectedStub = (serverType == ServerType.GAME_SERVER)
                ? gameServerStub : chatServerStub;

        TransitionRequest request = TransitionRequest.newBuilder()
                .setAccountId(accountId)
                .build();

        selectedStub
                .notifyTransition(request, new StreamObserver<>() {
                    @Override
                    public void onNext(TransitionResponse response) {
                        callback.onSuccess(response.getSuccess());
                    }

                    @Override
                    public void onError(Throwable t) {
                        callback.onFailure(t);
                    }

                    @Override
                    public void onCompleted() {
                        callback.onCompleted();
                    }
                });
    }
}
