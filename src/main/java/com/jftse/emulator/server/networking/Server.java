package com.jftse.emulator.server.networking;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Log4j2
public class Server implements Runnable {
    private final int writeBufferSize, readBufferSize;
    private Selector selector;
    private ServerSocketChannel serverChannel;
    private final ConcurrentHashMap<Long, Connection> connections = new ConcurrentHashMap<>();
    private final ConnectionListener connectionListener;
    private volatile boolean shutdown;
    private final Object updateLock = new Object();
    private Thread updateThread;
    private int tcpPort;

    private ConnectionListener dispatchListener = new ConnectionListener() {
        public void connected(Connection connection) {
            try {
                Server.this.connectionListener.connected(connection);
            } catch (Exception ex) {
                log.error("OnConnected exception " + ex.getMessage(), ex);
            }
        }

        public void disconnected(Connection connection) {
            removeConnection(connection);
            try {
                Server.this.connectionListener.disconnected(connection);
            } catch (Exception ex) {
                log.error("OnDisconnected exception " + ex.getMessage(), ex);
            }
        }

        public void received(Connection connection, List<Packet> packets) {
            try {
                Server.this.connectionListener.received(connection, packets);
            } catch (Exception ex) {
                log.error("OnReceived exception " + ex.getMessage(), ex);
            }
        }

        public void idle(Connection connection) {
            try {
                Server.this.connectionListener.idle(connection);
            } catch (Exception ex) {
                log.error("OnIdle exception " + ex.getMessage(), ex);
            }
        }

        public void onException(Connection connection, Exception exception) {
            try {
                Server.this.connectionListener.onException(connection, exception);
            } catch (Exception ex) {
                log.error("OnException exception " + ex.getMessage(), ex);
            }
        }

        public void onTimeout(Connection connection) {
            Server.this.connectionListener.onTimeout(connection);
        }

        public void cleanUp() {
            // empty..
        }
    };

    public Server(ConnectionListener connectionListener) {
        this(16384, 16384, connectionListener);
    }

    public Server(int writeBufferSize, int readBufferSize, ConnectionListener connectionListener) {
        this.writeBufferSize = writeBufferSize;
        this.readBufferSize = readBufferSize;
        this.connectionListener = connectionListener;

        try {
            selector = Selector.open();
        } catch (IOException ioe) {
            throw new RuntimeException("Error opening selector.", ioe);
        }
    }

    public void restart() {

        try {
            selector = Selector.open();
        } catch (IOException ioe) {
            throw new RuntimeException("Error opening selector.", ioe);
        }
    }

    public void bind(int tcpPort) throws IOException {
        this.tcpPort = tcpPort;
        bind(new InetSocketAddress("0.0.0.0", tcpPort), null);
    }

    private void bind(InetSocketAddress tcpPort, InetSocketAddress udpPort) throws IOException {
        close();

        synchronized (updateLock) {
            selector.wakeup();

            try {
                serverChannel = selector.provider().openServerSocketChannel();
                serverChannel.socket().setReuseAddress(true);
                serverChannel.socket().bind(tcpPort, 1000);
                serverChannel.configureBlocking(false);
                serverChannel.register(selector, SelectionKey.OP_ACCEPT);
            } catch (IOException ioe) {

                close();
                throw ioe;
            }
        }
    }

    private void update(int timeout) throws IOException {

        updateThread = Thread.currentThread();

        synchronized (updateLock) {
        }

        int select;

        if (timeout > 0) {
            select = selector.select(timeout);
        } else {
            select = selector.selectNow();
        }

        if (select != 0) {
            Set<SelectionKey> keys = selector.selectedKeys();

            synchronized (keys) {
                for (Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext(); ) {
                    // keepAlive();
                    SelectionKey selectionKey = iter.next();
                    iter.remove();
                    Connection fromConnection = (Connection) selectionKey.attachment();

                    try {
                        if (!selectionKey.isValid())
                            continue;

                        int ops = selectionKey.readyOps();

                        if (fromConnection != null) {
                            if ((ops & SelectionKey.OP_READ) == SelectionKey.OP_READ) {
                                try {
                                    while (true) {

                                        List<Packet> packets = fromConnection.getTcpConnection().readPacket();
                                        if (packets == null || packets.isEmpty())
                                            break;
                                        fromConnection.notifyReceived(packets);
                                    }
                                } catch (IOException ioe) {
                                    fromConnection.notifyException(ioe);
                                    fromConnection.close();
                                }
                            }
                            if ((ops & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE) {

                                try {
                                    fromConnection.getTcpConnection().writeOperation();
                                } catch (IOException ioe) {
                                    fromConnection.notifyException(ioe);
                                    fromConnection.close();
                                }
                            }
                            continue;
                        }
                        if ((ops & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) {
                            ServerSocketChannel serverSocketChannel = this.serverChannel;
                            if (serverSocketChannel == null)
                                continue;

                            try {
                                SocketChannel socketChannel = serverSocketChannel.accept();
                                if (socketChannel != null)
                                    acceptOperation(socketChannel);
                            } catch (IOException ioe) {
                                log.error(ioe.getMessage());
                            }
                            continue;
                        }
                        selectionKey.channel().close();

                    } catch (CancelledKeyException cke) {
                        if (fromConnection != null) {
                            fromConnection.notifyException(cke);
                            fromConnection.close();
                        } else {
                            selectionKey.channel().close();
                        }
                    }
                }
            }
        }

        long time = System.currentTimeMillis();
        this.connections.forEach((id, connection) -> {
            if (connection.getTcpConnection().isTimedOut(time)) {
                connection.notifyTimeout();
            } /*else {
                if (connection.getTcpConnection().needsKeepAlive(time)) {
                    connection.sendTCP(new Packet(PacketOperations.C2SHeartbeat.getValueAsChar()));
                }
            }*/
            if (connection.isIdle()) {
                connection.notifyIdle();
            }
        });
    }

    private void keepAlive() {
        long time = System.currentTimeMillis();
        this.connections.forEach((id, connection) -> {
            if (connection.getTcpConnection().needsKeepAlive(time)) {
                connection.sendTCP(new Packet(PacketOperations.C2SHeartbeat.getValueAsChar()));
            }
        });
    }

    public void run() {
        shutdown = false;
        while (!shutdown) {
            try {
                update(10);
            } catch (IOException ioe) {
                log.error("Thread exception " + ioe.getMessage(), ioe);
                close();
            }
        }
    }

    public void start(String name) {
        Thread t = new Thread(this, name);
        t.setUncaughtExceptionHandler((t1, e) -> log.error("Uncaught exception in " + name + " thread. ", e));
        t.start();
    }

    public void stop() {
        if (!shutdown) {
            close();
            shutdown = true;
        }
    }

    private void acceptOperation(SocketChannel socketChannel) {
        Connection connection = newConnection();
        connection.initialize(this.writeBufferSize, this.readBufferSize);

        try {
            SelectionKey selectionKey = connection.getTcpConnection().accept(selector, socketChannel);
            selectionKey.attach(connection);

            long id = addConnection(connection);
            connection.setId(id);
            connection.setConnected(true);

            connection.notifyConnected();
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            connection.close();
        }
    }

    protected Connection newConnection() {
        return new Connection(this, dispatchListener);
    }

    private long addConnection(Connection connection) {
        long id = 1;
        while (connections.putIfAbsent(id, connection) != null) {
            id++;
        }
        return id;
    }

    private void removeConnection(Connection connection) {
        connections.remove(connection.getId());
    }

    public void sendToAllTcp(Packet packet) {
        this.connections.forEach((id, connection) -> connection.sendTCP(packet));
    }

    public void sendToTcp(long connectionId, Packet packet) {
        this.connections.forEach((id, connection) -> {
            if (connection.getId() == connectionId) {
                connection.sendTCP(packet);
            }
        });
    }

    private void close() {
        this.connections.forEach((id, connection) -> connection.close());
        this.connections.clear();

        ServerSocketChannel serverSocketChannel = this.serverChannel;

        if (serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException ioe) {
                // empty..
            }
            this.serverChannel = null;
        }

        synchronized (updateLock) {
        }

        selector.wakeup();
        try {
            selector.selectNow();
        } catch (IOException ioe) {
            // empty..
        }
    }

    public void dispose() throws IOException {
        if (!shutdown) {
            shutdown = true;

            close();
            selector.close();
        }
    }

    public Thread getUpdateThread() {
        return updateThread;
    }
}
