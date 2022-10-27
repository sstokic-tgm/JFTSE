package com.jftse.server.core.net;

import com.jftse.server.core.protocol.Packet;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;

import java.net.InetSocketAddress;

public abstract class Connection<T extends Client<?>> {
    protected ChannelHandlerContext chx;

    protected final int decryptionKey;
    protected final int encryptionKey;

    protected Connection(final int decryptionKey, final int encryptionKey) {
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
    }

    protected Connection(ChannelHandlerContext chx, final int decryptionKey, final int encryptionKey) {
        this.chx = chx;
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
    }

    public InetSocketAddress getRemoteAddressTCP() {
        return (InetSocketAddress) chx.channel().remoteAddress();
    }

    public ChannelFuture close() {
        return chx.close();
    }

    public final int getDecryptionKey() {
        return decryptionKey;
    }

    public final int getEncryptionKey() {
        return encryptionKey;
    }

    public abstract <C extends Client<?>> C getClient();

    public abstract void sendTCP(Packet... packets);
}
