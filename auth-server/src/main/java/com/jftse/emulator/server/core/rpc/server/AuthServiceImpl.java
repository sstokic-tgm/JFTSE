package com.jftse.emulator.server.core.rpc.server;

import com.google.protobuf.Empty;
import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.proto.auth.AuthServiceGrpc;
import com.jftse.proto.auth.UpdateAccountRequest;
import com.jftse.server.core.util.Time;
import io.grpc.stub.StreamObserver;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@Log4j2
public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    @Override
    public void updateAccount(UpdateAccountRequest request, StreamObserver<Empty> responseObserver) {
        final long start = Time.getNSTime();

        log.debug("[Account:{}] Received update request with timestamp={}",
                request.getAccountId(),
                request.getTimestamp());

        try {
            AuthenticationManager.getInstance().addUpdateAccountRequest(request);

            final long duration = Time.getNSTimeDiffToNow(start);
            log.debug("[Account:{}] Update request queued successfully in {} ms",
                    request.getAccountId(),
                    Time.nanoToMillis(duration));

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("[Account:{}] Failed to process update request: {}",
                    request.getAccountId(),
                    e.getMessage(),
                    e);

            responseObserver.onError(e);
        }
    }
}