package com.jftse.server.core.net;

import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.packets.SMSGDisconnectMessage;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;
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
        packetReceived(connection, packet);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("({}) Channel Inactive", remoteAddress);

        // channelInactive is only called once anyway
        connection.wantsToCloseConnection();
        disconnected0(connection);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "IP_UNKNOWN";

        if (!(cause instanceof ReadTimeoutException)) {
            var isConnectionResetError = switch (cause.getMessage()) {
                case "Connection reset", "Connection timed out", "No route to host" -> true;
                default -> false;
            };

            if (isConnectionResetError) {
                log.warn("({}) Client closed connection abruptly: {}", remoteAddress, cause.getMessage());
            } else {
                log.error("({}) exceptionCaught: {}", remoteAddress, cause.getMessage(), cause);
            }
        } else {
            log.warn("({}) Read timeout, closing connection.", remoteAddress);
        }

        exceptionCaught(connection, cause);

        if (cause instanceof DataAccessException) {
            SMSGDisconnectMessage dcPacket = SMSGDisconnectMessage.builder()
                    .result((byte) 0)
                    .build();
            connection.sendTCP(dcPacket).addListener((f) -> {
                if (f.isSuccess()) {
                    connection.close();
                } else {
                    log.warn("Failed to send disconnect packet to client before closing connection", f.cause());
                    connection.close();
                }
            });
        }
    }

    protected void disconnected0(T connection) {
        if (connection.getClient() != null) { // only call disconnected if the connection was fully established
            disconnected(connection);
        }
    }

    protected void packetProcessed(T connection, IPacket packet) {
        try {
            PacketHandler<T, IPacket> handler = PacketRegistry.getHandler(packet.getPacketId());
            if (handler != null) {
                handler.handle(connection, packet);
            } else {
                log.warn("No handler for packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId());
            }
        } catch (Exception e) {
            log.error("Error processing packet id: 0x{} ({})", Integer.toHexString(packet.getPacketId()), (int) packet.getPacketId(), e);
        }
    }

    protected abstract void connected(T connection);
    protected abstract void disconnected(T connection);
    protected abstract void packetReceived(T connection, IPacket packet);
    protected abstract void exceptionCaught(T connection, Throwable cause) throws Exception;
}
