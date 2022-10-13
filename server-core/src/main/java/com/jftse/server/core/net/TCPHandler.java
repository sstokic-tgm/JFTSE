package com.jftse.server.core.net;

import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public abstract class TCPHandler extends SimpleChannelInboundHandler<Packet> {
    protected final int decryptionKey;
    protected final int encryptionKey;

    private final PacketHandlerFactory packetHandlerFactory;

    protected TCPHandler(final int decryptionKey, final int encryptionKey, final PacketHandlerFactory packetHandlerFactory) {
        this.decryptionKey = decryptionKey;
        this.encryptionKey = encryptionKey;
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
                channelRead1(ctx, packet);
            } catch (Exception e) {
                ctx.fireExceptionCaught(e);
            }
        } else {
            handlerNotFound(ctx, packet);
        }
    }

    protected abstract void channelRead1(ChannelHandlerContext ctx, Packet packet) throws Exception;
    protected abstract void handlerNotFound(ChannelHandlerContext ctx, Packet packet) throws Exception;
    protected void packetProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        // empty
    }
    protected void packetNotProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        // empty
    }
}
