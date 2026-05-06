package com.jftse.emulator.server.core.utils;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.CompletableFuture;

public class FutureUtils {
    public static <T> CompletableFuture<T> toCompletableFuture(ListenableFuture<T> listenableFuture) {
        CompletableFuture<T> completableFuture = new CompletableFuture<>();
        Futures.addCallback(listenableFuture,
                new FutureCallback<T>() {
                    @Override
                    public void onSuccess(T t) {
                        completableFuture.complete(t);
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        completableFuture.completeExceptionally(throwable);
                    }
                },
                MoreExecutors.directExecutor());
        return completableFuture;
    }
}
