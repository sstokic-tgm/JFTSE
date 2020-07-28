package com.ft.emulator.server.networking;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.*;

@Getter
@Setter
@Log4j2
public class Server implements Runnable {
    private final int writeBufferSize, objectBufferSize;
    private final Selector selector;
    private int emptySelects;
    private ServerSocketChannel serverChannel;
    private List<Connection> connections = new ArrayList<>();
    private Map<Integer, Connection> pendingConnections = new HashMap<>();
    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private Object listenerLock = new Object();
    private int nextConnectionID = 1;
    private volatile boolean shutdown;
    private Object updateLock = new Object();
    private Thread updateThread;

    private ConnectionListener dispatchListener = new ConnectionListener() {
            public void connected(Connection connection) {
                Server.this.connectionListeners.forEach(cl -> cl.connected(connection));
            }

            public void disconnected(Connection connection) {
                removeConnection(connection);
                Server.this.connectionListeners.forEach(cl -> cl.disconnected(connection));
            }

            public void received(Connection connection, Packet packet) {
                Server.this.connectionListeners.forEach(cl -> cl.received(connection, packet));
            }

            public void idle(Connection connection) {
                Server.this.connectionListeners.forEach(cl -> cl.idle(connection));
            }

            public void onException(Connection connection, Exception exception) {
                Server.this.connectionListeners.forEach(cl -> cl.onException(connection, exception));
            }
        };

    public Server() {
        this(4096, 4096);
    }

    public Server(int writeBufferSize, int objectBufferSize) {
        this.writeBufferSize = writeBufferSize;
        this.objectBufferSize = objectBufferSize;

        try {
            selector = Selector.open();
        } catch (IOException ioe) {
            throw new RuntimeException("Error opening selector.", ioe);
        }
    }

    public void bind(int tcpPort) throws IOException {
        bind(new InetSocketAddress("0.0.0.0", tcpPort), null);
    }

    private void bind(InetSocketAddress tcpPort, InetSocketAddress udpPort) throws IOException {
        close();

        synchronized (updateLock) {
            selector.wakeup();

            try {
                serverChannel = selector.provider().openServerSocketChannel();
                serverChannel.socket().bind(tcpPort);
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

        long startTime = System.currentTimeMillis();
        int select = 0;

        if (timeout > 0) {
            select = selector.select(timeout);
        }
        else {
            select = selector.selectNow();
        }

        if (select == 0) {
            ++emptySelects;
            if (emptySelects == 100) {

                emptySelects = 0;
                long elapsedTime = System.currentTimeMillis() - startTime;

                try {
                    if (elapsedTime < 25)
                        Thread.sleep(25 - elapsedTime);
                } catch (InterruptedException ie) {
                    log.error(ie.getMessage());
                }
            }
        } else {

            emptySelects = 0;
            Set<SelectionKey> keys = selector.selectedKeys();

            synchronized (keys) {
                for(Iterator<SelectionKey> iter = keys.iterator(); iter.hasNext();) {

                    // keepAlive();
                    SelectionKey selectionKey = iter.next();
                    iter.remove();
                    Connection fromConnection = (Connection)selectionKey.attachment();

                    try {
                        if (fromConnection != null) {
                            if(selectionKey.isReadable()) {
                                try {

                                    while (true) {

                                        Packet packet = fromConnection.getTcpConnection().readPacket();
                                        if(packet == null)
                                            break;
                                        fromConnection.notifyReceived(packet);
                                    }
                                } catch (IOException ioe) {
                                    fromConnection.notifyException(ioe);
                                    fromConnection.close();
                                }
                            } else if (selectionKey.isWritable()) {

                                try {
                                    fromConnection.getTcpConnection().writeOperation();
                                } catch (IOException ioe) {
                                    fromConnection.notifyException(ioe);
                                    fromConnection.close();
                                }
                            }
                            continue;
                        }

                        if (selectionKey.isAcceptable()) {
                            ServerSocketChannel serverSocketChannel = this.serverChannel;

                            if (serverSocketChannel != null) {
                                try {
                                    SocketChannel socketChannel = serverSocketChannel.accept();
                                    if (socketChannel != null)
                                        acceptOperation(socketChannel);
                                }
                                catch (IOException ioe) {
                                    fromConnection.notifyException(ioe);
                                }
                            }
                            continue;
                        }
                        selectionKey.channel().close();

                    } catch (CancelledKeyException cke) {
                        fromConnection.notifyException(cke);

                        if(fromConnection != null)
                            fromConnection.close();
                        else
                            selectionKey.channel().close();
                    }
                }
            }
        }

        long time = System.currentTimeMillis();
        List<Connection> connections = this.connections;

        for (int i = 0; i < connections.size(); ++i) {
            Connection connection = connections.get(i);

            if (connection.getTcpConnection().isTimedOut(time)) {
                connection.close();
            }
            /* else {

               if (connection.getTcpConnection().needsKeepAlive(time))
               connection.sendTCP(new Packet((char) 0x0FA3));
               }*/
            if (connection.isIdle())
                connection.notifyIdle();
        }
    }

    private void keepAlive() {
        long time = System.currentTimeMillis();
        List<Connection> connections = this.connections;
        for (Connection connection : connections) {

            if (connection.getTcpConnection().needsKeepAlive(time))
                connection.sendTCP(new Packet((char) 0x0FA3));
        }
    }

    public void run() {
        shutdown = false;
        while(!shutdown) {
            try {
                update(10000);
            } catch (IOException ioe) {
                log.error(ioe.getMessage());
                close();
            }
        }
    }

    public void start(String name) {
        new Thread(this, name).start();
    }

    public void stop() {
        if(!shutdown) {
            close();
            shutdown = true;
        }
    }

    private void acceptOperation(SocketChannel socketChannel) {
        Connection connection = newConnection();
        connection.initialize(4096, 4096);
        connection.setServer(this);

        try {
            SelectionKey selectionKey = connection.getTcpConnection().accept(selector, socketChannel);
            selectionKey.attach(connection);

            int id = nextConnectionID++;
            if(nextConnectionID == -1)
                nextConnectionID = 1;

            connection.setId(id);
            connection.setConnected(true);
            connection.addConnectionListener(dispatchListener);

            addConnection(connection);
            connection.notifyConnected();
        } catch (IOException ioe) {
            log.error(ioe.getMessage());
            connection.close();
        }
    }

    protected Connection newConnection() {
        return new Connection();
    }

    private void addConnection(Connection connection) {
        connections.add(connection);
    }

    private void removeConnection(Connection connection) {
        connections.remove(connection);
    }

    public void sendToAllTcp(Packet packet) {
        for (Connection connection : connections) {
            connection.sendTCP(packet);
        }
    }

    public void sendToTcp(int connectionId, Packet packet) {
        for(Connection connection : connections) {
            if(connection.getId() == connectionId) {
                connection.sendTCP(packet);
                break;
            }
        }
    }

    public void addListener(ConnectionListener connectionListener) {
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
        connections.forEach(Connection::close);
        connections.clear();

        ServerSocketChannel serverSocketChannel = this.serverChannel;

        if(serverSocketChannel != null) {
            try {
                serverSocketChannel.close();
            } catch (IOException ioe) {
                // empty..
            }
            this.serverChannel = null;
        }

        synchronized (updateLock) { }

        selector.wakeup();
        try {
            selector.selectNow();
        } catch (IOException ioe) {
            // empty..
        }
    }

    public void dispose() throws IOException {

        close();
        selector.close();
    }

    public Thread getUpdateThread() {

        return updateThread;
    }
}
