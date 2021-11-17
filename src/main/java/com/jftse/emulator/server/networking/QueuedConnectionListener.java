package com.jftse.emulator.server.networking;

import com.jftse.emulator.server.networking.packet.Packet;

public abstract class QueuedConnectionListener implements ConnectionListener {
    private final ConnectionListener connectionListener;

    public QueuedConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }

    public void connected(Connection connection) {
        queue(() -> connectionListener.connected(connection));
    }

    public void disconnected(Connection connection) {
        queue(() -> connectionListener.disconnected(connection));
    }

    public void received(Connection connection, Packet packet) {
        queue(() -> connectionListener.received(connection, packet));
    }

    public void idle(Connection connection) {
        queue(() -> connectionListener.idle(connection));
    }

    public void onException(Connection connection, Exception exception) {
        queue(() -> connectionListener.onException(connection, exception));
    }

    public void onTimeout(Connection connection) {
        queue(() -> connectionListener.onTimeout(connection));
    }

    public void cleanUp() {
        connectionListener.cleanUp();
    }

    abstract protected void queue(Runnable runnable);
}
