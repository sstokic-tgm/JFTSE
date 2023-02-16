package com.jftse.server.core.net;

import com.jftse.entities.database.model.log.ServerType;
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

    protected AtomicBoolean isClosingConnection = new AtomicBoolean(false);

    protected final int decryptionKey;
    protected final int encryptionKey;

    protected ServerType serverType;

    protected Connection(final int decryptionKey, final int encryptionKey) {
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
    }

    protected Connection(ChannelHandlerContext ctx, final int decryptionKey, final int encryptionKey) {
        this.ctx = ctx;
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
    }

    public InetSocketAddress getRemoteAddressTCP() {
        return (InetSocketAddress) ctx.channel().remoteAddress();
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

    public void setChannelHandlerContext(ChannelHandlerContext chx) {
        this.ctx = chx;
        setId(this.ctx.channel().id());
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return ctx;
    }

    public void setId(ChannelId id) {
        this.id = id;
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

    public void setServerType(ServerType serverType) {
        this.serverType = serverType;
    }

    public ServerType getServerType() {
        return serverType;
    }

    public abstract void sendTCP(Packet... packets);
}
