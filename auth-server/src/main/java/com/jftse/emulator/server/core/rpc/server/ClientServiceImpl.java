package com.jftse.emulator.server.core.rpc.server;

import com.google.protobuf.Empty;
import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.proto.auth.ClientServiceGrpc;
import com.jftse.proto.auth.FTClientList;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.concurrent.ConcurrentLinkedDeque;

@GrpcService
public class ClientServiceImpl extends ClientServiceGrpc.ClientServiceImplBase {
    @Override
    public void getClientList(Empty request, StreamObserver<FTClientList> responseObserver) {
        final ConcurrentLinkedDeque<FTClient> clients = AuthenticationManager.getInstance().getClients();

        FTClientList.Builder clientListBuilder = FTClientList.newBuilder();
        for (final FTClient client : clients) {
            final Long accountId = client.getAccountId();
            if (accountId == null)
                continue;

            com.jftse.proto.auth.FTClient ftClient = com.jftse.proto.auth.FTClient.newBuilder()
                    .setAccountId(accountId)
                    .build();

            clientListBuilder.addClient(ftClient);
        }

        FTClientList clientList = clientListBuilder.build();
        responseObserver.onNext(clientList);
        responseObserver.onCompleted();
    }
}
