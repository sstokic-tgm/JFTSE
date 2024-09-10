package com.jftse.server.core.proto.interfaces;

public interface TransitionCallback {
    void onSuccess(boolean success);
    void onFailure(Throwable t);
    void onCompleted();
}
