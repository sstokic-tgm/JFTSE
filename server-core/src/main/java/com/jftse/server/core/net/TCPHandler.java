package com.jftse.server.core.net;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.S2CDCMsgPacket;
import com.jftse.server.core.thread.ThreadManager;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

public abstract class TCPHandler<T extends Connection<? extends Client<T>>> extends SimpleChannelInboundHandler<Packet> {
    private final Logger log = LogManager.getLogger(getClass());

    protected final AttributeKey<T> CONNECTION_ATTRIBUTE_KEY;
    private final PacketHandlerFactory packetHandlerFactory;

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
                if (packet.getPacketId() == PacketOperations.C2SMatchplayPlayerUseSkill.getValue()) {
                    ThreadManager.getInstance().newTask(() -> {
                        try {
                            abstractPacketHandler.setConnection(connection);
                            if (abstractPacketHandler.process(packet)) {
                                abstractPacketHandler.handle();
                                packetProcessed(connection, abstractPacketHandler);
                            } else {
                                packetNotProcessed(connection, abstractPacketHandler);
                            }
                        } catch (Exception e) {
                            try {
                                exceptionCaught(ctx, e);
                            } catch (Exception ex) {
                                ctx.close();
                            }
                        }
                    });
                    return;
                }
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

            if (cause instanceof DataAccessException) {
                S2CDCMsgPacket dcPacket = new S2CDCMsgPacket(0);
                connection.sendTCP(dcPacket);
            }

            connection.close();
        }
    }

    public void packetProcessed(T connection, AbstractPacketHandler handler) throws Exception {
        // empty
    }

    public void packetNotProcessed(T connection, AbstractPacketHandler handler) throws Exception {
        // empty
    }

    public abstract void connected(T connection);

    public abstract void disconnected(T connection);

    public abstract void exceptionCaught(T connection, Throwable cause) throws Exception;

    public abstract void handlerNotFound(T connection, Packet packet) throws Exception;
}
