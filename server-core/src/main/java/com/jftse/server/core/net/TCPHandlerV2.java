package com.jftse.server.core.net;

import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.shared.packets.SMSGDisconnectMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import java.net.InetSocketAddress;

public abstract class TCPHandlerV2<T extends Connection<? extends Client<T>>> extends SimpleChannelInboundHandler<IPacket> {
    protected final AttributeKey<T> CONNECTION_ATTRIBUTE_KEY;
    private final Logger log = LogManager.getLogger(getClass());

    protected TCPHandlerV2(final AttributeKey<T> connectionAttributeKey) {
        this.CONNECTION_ATTRIBUTE_KEY = connectionAttributeKey;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        connection.setChannelHandlerContext(ctx);

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("({}) Channel Active", remoteAddress);

        connected(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, IPacket packet) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        if (connection != null && !connection.getIsClosingConnection().get()) {
            packetReceived(connection, packet);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("({}) Channel Inactive", remoteAddress);

        if (!connection.getIsClosingConnection().get()) {
            connection.wantsToCloseConnection();
            disconnected0(connection);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        if (connection != null) {
            exceptionCaught(connection, cause);

            if (cause instanceof DataAccessException) {
                SMSGDisconnectMessage dcPacket = SMSGDisconnectMessage.builder()
                        .result((byte) 0)
                        .build();
                connection.sendTCP(dcPacket).addListener((f) -> {
                    if (f.isSuccess()) {
                        connection.close();
                    }else {
                        log.warn("Failed to send disconnect packet to client before closing connection", f.cause());
                        connection.close();
                    }
                });
            }
        }
    }

    protected void disconnected0(T connection) {
        if (connection.getClient() != null) { // only call disconnected if the connection was fully established
            disconnected(connection);
        }
    }

    protected abstract void connected(T connection);
    protected abstract void disconnected(T connection);
    protected abstract void packetReceived(T connection, IPacket packet);
    protected abstract void exceptionCaught(T connection, Throwable cause) throws Exception;
}
