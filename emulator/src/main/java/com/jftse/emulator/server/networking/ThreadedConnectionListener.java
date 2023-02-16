package com.jftse.emulator.server.networking;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadedConnectionListener extends QueuedConnectionListener {
    protected final ExecutorService threadPool;

    public ThreadedConnectionListener(ConnectionListener connectionListener) {
        this(connectionListener, Executors.newFixedThreadPool(1));
    }

    public ThreadedConnectionListener(ConnectionListener connectionListener, ExecutorService threadPool) {
        super(connectionListener);
        this.threadPool = threadPool;
    }

    public void queue(Runnable runnable) {
        threadPool.execute(runnable);
    }
}
