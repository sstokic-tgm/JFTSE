package com.jftse.emulator.server.core.rpc.interceptor;

import io.grpc.*;
import net.devh.boot.grpc.server.interceptor.GrpcGlobalServerInterceptor;

import java.util.concurrent.TimeUnit;

@GrpcGlobalServerInterceptor
public class CacheControlServerInterceptor implements ServerInterceptor {
    private static final long CACHE_MAX_AGE_SECONDS = TimeUnit.SECONDS.convert(30, TimeUnit.SECONDS);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> serverCall, Metadata metadata, ServerCallHandler<ReqT, RespT> serverCallHandler) {
        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(serverCallHandler.startCall(serverCall, metadata)) {
            @Override
            public void onHalfClose() {
                metadata.put(Metadata.Key.of("cache-control", Metadata.ASCII_STRING_MARSHALLER), "max-age=" + CACHE_MAX_AGE_SECONDS);
                super.onHalfClose();
            }
        };
    }
}
