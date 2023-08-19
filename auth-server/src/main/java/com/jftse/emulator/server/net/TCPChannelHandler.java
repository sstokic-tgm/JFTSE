package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.account.Account;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.impl.AuthenticationServiceImpl;
import com.jftse.server.core.shared.packets.S2CWelcomePacket;
import io.netty.channel.ChannelHandler;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.AttributeKey;
import lombok.extern.log4j.Log4j2;

import java.net.InetSocketAddress;

@Log4j2
@ChannelHandler.Sharable
public class TCPChannelHandler extends TCPHandler<FTConnection> {
    private final BlockedIPService blockedIPService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(ftConnectionAttributeKey, phf);

        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
    }

    @Override
    public void connected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Active");

        if (!checkIp(connection, remoteAddress, () -> blockedIPService, () -> log))
            return;

        FTClient client = new FTClient();

        client.setConnection(connection);
        connection.setClient(client);

        AuthenticationManager.getInstance().addClient(client);

        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecryptionKey(), connection.getEncryptionKey(), 0, 0);
        connection.sendTCP(welcomePacket);
    }

    @Override
    public void handlerNotFound(FTConnection connection, Packet packet) throws Exception {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.warn("(" + remoteAddress + ") There is no implementation registered for " + PacketOperations.getNameByValue(packet.getPacketId()) + " packet (id " + String.format("0x%X", (int) packet.getPacketId()) + ")");
    }

    @Override
    public void packetNotProcessed(FTConnection connection, AbstractPacketHandler handler) throws Exception {
        log.warn(handler.getClass().getSimpleName() + " packet has not been processed");
    }

    @Override
    public void disconnected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Inactive");

        FTClient client = connection.getClient();
        if (client != null) {
            Account account = client.getAccount();
            if (account != null && account.getStatus() != AuthenticationServiceImpl.ACCOUNT_BLOCKED_USER_ID) {
                account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                account.setLoggedInServer(null);
                client.saveAccount(account);
            }
            AuthenticationManager.getInstance().removeClient(client);
        }
    }

    @Override
    public void exceptionCaught(FTConnection connection, Throwable cause) throws Exception {
        if (!cause.equals(ReadTimeoutException.INSTANCE)) {
            var shouldHandleException = switch (cause.getMessage()) {
                case "Connection reset", "Connection timed out", "No route to host" -> false;
                default -> true;
            };

            if (shouldHandleException) {
                InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
                String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
                log.warn("(" + remoteAddress + ") exceptionCaught: " + cause.getMessage(), cause);
            }
        }
    }
}
