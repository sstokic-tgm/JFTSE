package com.ft.emulator.server.networking;

import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Connection {

    private int id = -1;
    private String name;
    private Server server;
    private TcpConnection tcpConnection;
    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private Object listenerLock = new Object();
    private volatile boolean isConnected;

    private Client client;

    protected Connection() { }

    public void initialize(int writeBufferSize, int objectBufferSize) {
        tcpConnection = new TcpConnection(writeBufferSize, objectBufferSize);
    }

    public int getId() {
        return id;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int sendTCP(Packet packet) {
        if(packet == null)
            throw new IllegalArgumentException("Packet cannot be null.");

        try {

            return tcpConnection.send(packet);
        } catch (IOException ioe) {
            close();
            return 0;
        }
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        if(connectionListener == null)
            throw new IllegalArgumentException("ConnectionListener cannot be null.");

        synchronized (listenerLock) {
            connectionListeners.add(connectionListener);
        }
    }

    public void removeListener(ConnectionListener connectionListener) {
        if(connectionListener == null)
            throw new IllegalArgumentException("ConnectionListener cannot be null.");

        synchronized (listenerLock) {
            connectionListeners.remove(connectionListener);
        }
    }

    public void close() {
        boolean wasConnected = isConnected;
        isConnected = false;
        tcpConnection.close();

        if(wasConnected)
            notifyDisconnected();

        setConnected(false);
    }

    public void notifyConnected() {
        connectionListeners.forEach(cl -> cl.connected(this));
    }

    public void notifyDisconnected() {
        connectionListeners.forEach(cl -> cl.disconnected(this));
    }

    public void notifyReceived(Packet packet) {
        connectionListeners.forEach(cl -> cl.received(this, packet));
    }

    public void notifyIdle() {
        for(ConnectionListener cl : connectionListeners) {
            cl.idle(this);
            if(!isIdle())
                break;
        }
    }

    public InetSocketAddress getRemoteAddressTCP() {
        SocketChannel socketChannel = tcpConnection.getSocketChannel();

        if(socketChannel != null) {
            Socket socket = socketChannel.socket();
            if(socket != null) {
                return (InetSocketAddress)socket.getRemoteSocketAddress();
            }
        }
        return null;
    }

    public boolean isIdle() {
        return tcpConnection.getWriteBuffer().position() / (float)tcpConnection.getWriteBuffer().capacity() < tcpConnection.getIdleThresHold();
    }

    public void setIdleThreshold(float idleThreshold) {
        tcpConnection.setIdleThresHold(idleThreshold);
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
        if(isConnected && name == null)
            name = "Connection " + id;
    }
}
