package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import io.netty.channel.ChannelHandler;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandler<FTConnection> {
    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(ftConnectionAttributeKey, phf);
    }

    @Override
    public void connected(FTConnection connection) {
        String remoteAddress = connection.getRemoteAddressTCP().toString();
        log.info("(" + remoteAddress + ") Channel Active");

        if (!checkIp(connection, remoteAddress, () -> log))
            return;

        FTClient client = new FTClient();

        client.setConnection(connection);
        connection.setClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    public void handlerNotFound(FTConnection connection, Packet packet) throws Exception {
        log.warn("(" + connection.getRemoteAddressTCP().toString() + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    public void packetNotProcessed(FTConnection connection, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void disconnected(FTConnection connection) {
        log.info("(" + connection.getRemoteAddressTCP().toString() + ") Channel Inactive");

        FTClient client = connection.getClient();
        if (client != null) {
            client.getGameSessionId().ifPresent(sessionId -> RelayManager.getInstance().removeClient(sessionId, client));
        }
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        var shouldHandleException = switch (cause.getMessage()) {
            case "Connection reset", "Connection timed out", "No route to host" -> false;
            default -> true;
        };

        if (shouldHandleException) {
            log.warn("(" + connection.getRemoteAddressTCP().toString() + ") exceptionCaught: " + cause.getMessage(), cause);
        }
    }
}
