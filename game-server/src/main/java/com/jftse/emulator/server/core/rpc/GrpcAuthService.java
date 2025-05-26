package com.jftse.emulator.server.core.rpc;

import com.jftse.proto.auth.AuthServiceGrpc;
import com.jftse.proto.auth.UpdateAccountRequest;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
public class GrpcAuthService {
    @GrpcClient("auth-server")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    public void updateAccount(UpdateAccountRequest request) {
        authServiceBlockingStub.updateAccount(request);
    }
}
