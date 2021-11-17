package com.jftse.emulator.server.networking;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.SocketChannel;
import java.util.Random;
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

    private int decKey;
    private int encKey;

    protected Connection() {
        this.decKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;
        this.encKey = ConfigService.getInstance().getValue("network.encryption.enabled", false) ? getRandomBigInteger().intValueExact() : 0;
    }

    private BigInteger getRandomBigInteger() {
        Random rnd = new Random();
        BigInteger upperLimit = new BigInteger("10000");
        BigInteger result;
        do {
            result = new BigInteger(upperLimit.bitLength(), rnd);
        } while (result.compareTo(upperLimit) > 0);
        return result;
    }

    public void initialize(int writeBufferSize, int objectBufferSize) {
        tcpConnection = new TcpConnection(writeBufferSize, objectBufferSize, decKey, encKey);
    }

    public int getId() {
        return id;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public int sendTCP(Packet... packets) {
        if(packets == null || packets.length == 0)
            throw new IllegalArgumentException("Packet cannot be null.");

        try {

            return tcpConnection.send(packets);
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
        return Math.abs(tcpConnection.getLastReadTime() - tcpConnection.getLastWriteTime());
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
        if (wasConnected)
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
