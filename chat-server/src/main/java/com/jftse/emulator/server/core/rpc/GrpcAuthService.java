package com.jftse.emulator.server.core.rpc;

import com.jftse.proto.auth.AuthServiceGrpc;
import com.jftse.proto.auth.UpdateAccountRequest;
import io.grpc.StatusRuntimeException;
import lombok.extern.log4j.Log4j2;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class GrpcAuthService {
    @GrpcClient("auth-server")
    private AuthServiceGrpc.AuthServiceBlockingStub authServiceBlockingStub;

    public void updateAccount(UpdateAccountRequest request) {
        try {
            authServiceBlockingStub.updateAccount(request);
        } catch (StatusRuntimeException e) {
            log.warn("gRPC call failed: {}, Status: {}", e.getMessage(), e.getStatus(), e);
        } catch (Exception e) {
            log.error("Failed to update account: {}", e.getMessage(), e);
        }
    }
}
