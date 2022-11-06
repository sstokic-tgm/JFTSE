package com.jftse.server.core.net;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public abstract class TCPHandler extends SimpleChannelInboundHandler<Packet> {
    private final PacketHandlerFactory packetHandlerFactory;

    protected TCPHandler(final PacketHandlerFactory packetHandlerFactory) {
        this.packetHandlerFactory = packetHandlerFactory;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
        AbstractPacketHandler abstractPacketHandler = packetHandlerFactory.getHandler(PacketOperations.getPacketOperationByValue(packet.getPacketId()));
        if (abstractPacketHandler != null) {
            abstractPacketHandler.setConnection((Connection<?>) ctx.channel().attr(AttributeKey.valueOf("connection")).get());
            try {
                if (abstractPacketHandler.process(packet)) {
                    abstractPacketHandler.handle();
                    packetProcessed(ctx, abstractPacketHandler);
                } else {
                    packetNotProcessed(ctx, abstractPacketHandler);
                }
            } catch (Exception e) {
                exceptionCaught(ctx, e);
            }
        } else {
            handlerNotFound(ctx, packet);
        }
    }

    protected abstract void handlerNotFound(ChannelHandlerContext ctx, Packet packet) throws Exception;
    protected void packetProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        // empty
    }
    protected void packetNotProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        // empty
    }
}
