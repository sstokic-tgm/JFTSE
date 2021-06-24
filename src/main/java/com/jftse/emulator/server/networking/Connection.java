package com.jftse.emulator.server.networking;

import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedDeque;

@Getter
@Setter
@Log4j2
public class Connection {

    private int id = -1;
    private String name;
    private Server server;
    private TcpConnection tcpConnection;
    private ConcurrentLinkedDeque<ConnectionListener> connectionListeners = new ConcurrentLinkedDeque<>();
    private volatile boolean isConnected;

    private String hwid;
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
            switch ("" + ioe.getMessage()) {
                case "Connection is closed.":
                case "Connection reset by peer":
                case "Broken pipe":
                    break;
                default:
                    log.error("Unable to send packet " + ioe.getMessage(), ioe);
            }
            close();
            return 0;
        }
    }

    public long getLatency() {
        return Math.abs(System.currentTimeMillis() - tcpConnection.getLastReadTime());
    }

    public void addConnectionListener(ConnectionListener connectionListener) {
        if(connectionListener == null)
            throw new IllegalArgumentException("ConnectionListener cannot be null.");

        connectionListeners.add(connectionListener);
    }

    public void removeListener(ConnectionListener connectionListener) {
        if(connectionListener == null)
            throw new IllegalArgumentException("ConnectionListener cannot be null.");

        connectionListeners.remove(connectionListener);
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

    public void notifyException(Exception exception) {
        connectionListeners.forEach(cl -> cl.onException(this, exception));
    }

    public void notifyTimeout() {
        connectionListeners.forEach(cl -> cl.onTimeout(this));
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
