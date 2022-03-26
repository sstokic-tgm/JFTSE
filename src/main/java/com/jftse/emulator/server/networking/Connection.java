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
import java.util.List;
import java.util.Random;

@Getter
@Setter
@Log4j2
public class Connection {

    private long id = -1;
    private String name;
    private final Server server;
    private TcpConnection tcpConnection;
    private final ConnectionListener connectionListener;
    private volatile boolean isConnected;
    private InetSocketAddress inetSocketAddress;

    private String hwid;
    private Client client;

    private final int decKey;
    private final int encKey;

    protected Connection(Server server, ConnectionListener connectionListener) {
        this.server = server;
        this.connectionListener = connectionListener;

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

    public void initialize(int writeBufferSize, int readBufferSize) {
        tcpConnection = new TcpConnection(writeBufferSize, readBufferSize, decKey, encKey);
        this.inetSocketAddress = getRemoteAddressTCP();
    }

    public long getId() {
        return id;
    }

    public synchronized boolean isConnected() {
        return isConnected;
    }

    public synchronized int sendTCP(Packet... packets) {
        if (packets == null || packets.length == 0)
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

    public synchronized long getLatency() {
        return Math.abs(tcpConnection.getLastReadTime() - tcpConnection.getLastWriteTime());
    }

    public synchronized void close() {
        boolean wasConnected = isConnected;
        isConnected = false;
        tcpConnection.close();
        if (wasConnected)
            notifyDisconnected();

        setConnected(false);
    }

    public void notifyConnected() {
        connectionListener.connected(this);
    }

    public void notifyDisconnected() {
        connectionListener.disconnected(this);
    }

    public void notifyReceived(List<Packet> packets) {
        connectionListener.received(this, packets);
    }

    public void notifyIdle() {
        connectionListener.idle(this);
    }

    public void notifyException(Exception exception) {
        connectionListener.onException(this, exception);
    }

    public void notifyTimeout() {
        connectionListener.onTimeout(this);
    }

    public InetSocketAddress getRemoteAddressTCP() {
        if (this.inetSocketAddress == null) {
            SocketChannel socketChannel = tcpConnection.getSocketChannel();

            if (socketChannel != null) {
                Socket socket = socketChannel.socket();
                if (socket != null) {
                    this.inetSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
                    return this.inetSocketAddress;
                }
            }
            return null;
        } else {
            return this.inetSocketAddress;
        }
    }

    public boolean isIdle() {
        return tcpConnection.getWriteBuffer().position() / (float) tcpConnection.getWriteBuffer().capacity() < tcpConnection.getIdleThresHold();
    }

    public void setIdleThreshold(float idleThreshold) {
        tcpConnection.setIdleThresHold(idleThreshold);
    }

    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
        if (isConnected && name == null)
            name = "Connection " + id;
    }
}
