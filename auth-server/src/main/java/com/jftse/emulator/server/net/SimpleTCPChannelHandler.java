package com.jftse.emulator.server.net;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class SimpleTCPChannelHandler extends TCPHandler {
    private FTConnection connection;

    private static final AttributeKey<FTConnection> FT_CONNECTION_ATTRIBUTE_KEY = AttributeKey.newInstance("connection");

    public SimpleTCPChannelHandler(final int decryptionKey, final int encryptionKey, final PacketHandlerFactory phf) {
        super(decryptionKey, encryptionKey, phf);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        log.info("(" + ctx.channel().remoteAddress() + ") Channel Active");
        connection = new FTConnection(ctx, decryptionKey, encryptionKey);
        FTClient client = new FTClient();

        client.setConnection(connection);
        connection.setClient(client);

        ctx.channel().attr(FT_CONNECTION_ATTRIBUTE_KEY).set(connection);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    protected void channelRead1(ChannelHandlerContext ctx, Packet packet) throws Exception {
        log.info("(" + ctx.channel().remoteAddress() + ") SimpleTCPChannelHandler: " + BitKit.toString(packet.getRawPacket(), 0, packet.getRawPacket().length));
    }

    @Override
    protected void handlerNotFound(ChannelHandlerContext ctx, Packet packet) throws Exception {
        log.warn("(" + ctx.channel().remoteAddress() + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    protected void packetNotProcessed(ChannelHandlerContext ctx, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.info("(" + ctx.channel().remoteAddress() + ") Channel Inactive");

        if (connection != null && connection.getClient() != null) {
            Account account = connection.getClient().getAccount();
            if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                connection.getClient().saveAccount(account);
            }
            connection.setClient(null);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("(" + ctx.channel().remoteAddress() + ") exceptionCaught: " + cause.getMessage());
    }
}
