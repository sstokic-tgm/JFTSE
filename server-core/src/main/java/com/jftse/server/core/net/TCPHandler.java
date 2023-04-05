package com.jftse.server.core.net;

import com.jftse.entities.database.model.log.BlockedIP;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BlockedIPService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class TCPHandler<T extends Connection<? extends Client<T>>> extends SimpleChannelInboundHandler<Packet> {
    protected final AttributeKey<T> CONNECTION_ATTRIBUTE_KEY;
    private final PacketHandlerFactory packetHandlerFactory;

    private final Map<String, Pair<Long, Byte>> tracker = new ConcurrentHashMap<>();

    protected TCPHandler(final AttributeKey<T> connectionAttributeKey, final PacketHandlerFactory packetHandlerFactory) {
        this.CONNECTION_ATTRIBUTE_KEY = connectionAttributeKey;
        this.packetHandlerFactory = packetHandlerFactory;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();
        connection.setChannelHandlerContext(ctx);

        connected(connection);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        AbstractPacketHandler abstractPacketHandler = packetHandlerFactory.getHandler(PacketOperations.getPacketOperationByValue(packet.getPacketId()));
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        if (connection != null && !connection.getIsClosingConnection().get()) {
            if (abstractPacketHandler != null) {
                abstractPacketHandler.setConnection(connection);
                try {
                    if (abstractPacketHandler.process(packet)) {
                        abstractPacketHandler.handle();
                        packetProcessed(connection, abstractPacketHandler);
                    } else {
                        packetNotProcessed(connection, abstractPacketHandler);
                    }
                } catch (Exception e) {
                    exceptionCaught(ctx, e);
                }
            } else {
                handlerNotFound(connection, packet);
            }
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        if (connection != null && connection.getIsClosingConnection().compareAndSet(false, true)) {
            disconnected(connection);

            connection.setClient(null);
            ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).set(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        T connection = ctx.channel().attr(CONNECTION_ATTRIBUTE_KEY).get();

        if (connection != null) {
            exceptionCaught(connection, cause);

            channelInactive(ctx);
            connection.close();
        }
    }

    public void packetProcessed(T connection, AbstractPacketHandler handler) throws Exception {
        // empty
    }

    public void packetNotProcessed(T connection, AbstractPacketHandler handler) throws Exception {
        // empty
    }

    public boolean checkIp(T connection, String remoteAddress, Supplier<BlockedIPService> blockedIPServiceSupplier, Supplier<Logger> loggerSupplier) {
        String address = remoteAddress.substring(1, remoteAddress.lastIndexOf(":"));

        Logger log = loggerSupplier.get();
        BlockedIPService blockedIPService = blockedIPServiceSupplier.get();

        Optional<BlockedIP> ipOptional = blockedIPService.findBlockedIPByIpAndServerType(address, connection.getServerType());
        if (ipOptional.isPresent()) {
            connection.close();
            return false;
        }

        final Pair<Long, Byte> track = tracker.get(address);
        byte count;
        if (track == null) {
            count = 1;
        } else {
            count = track.getRight();

            final long difference = System.currentTimeMillis() - track.getLeft();
            if (difference < 7000) {
                count++;
            } else if (difference > 20000) {
                count = 1;
            }
            if (count >= 5) {
                log.info("adding to blocked ip: " + address);
                BlockedIP ipToBlock = new BlockedIP(address, connection.getServerType());
                blockedIPService.save(ipToBlock);
                tracker.remove(address);
                connection.close();
                return false;
            }
        }
        tracker.put(address, Pair.of(System.currentTimeMillis(), count));
        return true;
    }

    public abstract void connected(T connection);

    public abstract void disconnected(T connection);

    public abstract void exceptionCaught(T connection, Throwable cause) throws Exception;

    public abstract void handlerNotFound(T connection, Packet packet) throws Exception;
}
