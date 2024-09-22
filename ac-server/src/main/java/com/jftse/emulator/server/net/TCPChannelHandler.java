package com.jftse.emulator.server.net;

import com.jftse.emulator.server.core.manager.ACManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketHandlerFactory;
import com.jftse.server.core.net.TCPHandler;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.BlockedIPService;
import com.jftse.server.core.service.ClientWhitelistService;
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
    private final ClientWhitelistService clientWhitelistService;
    private final BlockedIPService blockedIPService;
    private final AuthenticationService authenticationService;

    public TCPChannelHandler(final AttributeKey<FTConnection> ftConnectionAttributeKey, final PacketHandlerFactory phf) {
        super(ftConnectionAttributeKey, phf);

        this.clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        this.blockedIPService = ServiceManager.getInstance().getBlockedIPService();
        this.authenticationService = ServiceManager.getInstance().getAuthenticationService();
    }

    @Override
    public void connected(FTConnection connection) {
        InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
        String remoteAddress = inetSocketAddress != null ? inetSocketAddress.toString() : "null";
        log.info("(" + remoteAddress + ") Channel Active");

        FTClient client = new FTClient();

        client.setIp(remoteAddress.substring(1, remoteAddress.lastIndexOf(":")));
        client.setPort(Integer.parseInt(remoteAddress.substring(remoteAddress.indexOf(":") + 1)));
        client.setConnection(connection);
        connection.setClient(client);

        ACManager.getInstance().addClient(client);

        ClientWhitelist clientWhitelist = new ClientWhitelist();
        clientWhitelist.setIp(client.getIp());
        clientWhitelist.setPort(client.getPort());
        clientWhitelist.setFlagged(false);
        clientWhitelist.setIsAuthenticated(false);
        clientWhitelist.setIsActive(true);
        clientWhitelistService.save(clientWhitelist);

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
            String hostAddress = client.getIp();
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, connection.getHwid());
            if (clientWhitelist != null) {
                clientWhitelist.setIsActive(false);
                clientWhitelist = clientWhitelistService.save(clientWhitelist);

                Account account = clientWhitelist.getAccount();
                if (account != null) {
                    account = authenticationService.findAccountById(account.getId());
                    if (account.getStatus() == AuthenticationServiceImpl.ACCOUNT_ALREADY_LOGGED_IN && clientWhitelist.getIsAuthenticated()) {
                        account.setStatus((int) AuthenticationServiceImpl.SUCCESS);
                        authenticationService.updateAccount(account);
                    }
                }
            }
            ACManager.getInstance().removeClient(client);
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
