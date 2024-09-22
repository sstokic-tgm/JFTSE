package com.jftse.server.core.net;

import com.jftse.entities.database.model.ServerType;
import com.jftse.server.core.protocol.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Connection<T extends Client<? extends Connection<T>>> {
    protected T client;
    protected ChannelHandlerContext ctx;
    protected ChannelId id;
    protected InetSocketAddress remoteAddress;

    protected AtomicBoolean isClosingConnection = new AtomicBoolean(false);

    protected final int decryptionKey;
    protected final int encryptionKey;
    protected final ServerType serverType;

    protected Connection(final int decryptionKey, final int encryptionKey, final ServerType serverType) {
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
        this.serverType = serverType;
    }

    public InetSocketAddress getRemoteAddressTCP() {
        if (ctx == null)
            return null;

        if (remoteAddress == null)
            remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();

        return remoteAddress;
    }

    public ChannelFuture close() {
        return ctx.close();
    }

    public final int getDecryptionKey() {
        return decryptionKey;
    }

    public final int getEncryptionKey() {
        return encryptionKey;
    }

    public void setChannelHandlerContext(ChannelHandlerContext ctx) {
        this.ctx = ctx;
        this.id = ctx.channel().id();
        this.remoteAddress = (InetSocketAddress) ctx.channel().remoteAddress();
    }

    public ChannelId getId() {
        return id;
    }

    public void setClient(T client) {
        this.client = client;
    }

    public T getClient() {
        return this.client;
    }

    public AtomicBoolean getIsClosingConnection() {
        return isClosingConnection;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public abstract ChannelFuture sendTCP(Packet... packets);
}
