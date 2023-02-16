package com.jftse.emulator.server.core.handler.anticheat;

import com.jftse.emulator.server.core.manager.AntiCheatManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.core.service.ClientWhitelistService;
import com.jftse.entities.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.networking.Connection;

import java.net.InetSocketAddress;

public class BasicAntiCheatHandler {
    private final ClientWhitelistService clientWhitelistService;

    public BasicAntiCheatHandler() {
        this.clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
    }

    public void sendWelcomePacket(Connection connection) {
        if (connection.getRemoteAddressTCP() != null) {
            InetSocketAddress inetSocketAddress = connection.getRemoteAddressTCP();
            String hostAddress = inetSocketAddress.getAddress().getHostAddress();
            int port = inetSocketAddress.getPort();

            connection.getClient().setIp(hostAddress);
            connection.getClient().setPort(port);

            ClientWhitelist clientWhitelist = new ClientWhitelist();
            clientWhitelist.setIp(hostAddress);
            clientWhitelist.setPort(port);
            clientWhitelist.setFlagged(false);
            clientWhitelist.setIsAuthenticated(false);
            clientWhitelist.setIsActive(true);
            clientWhitelistService.save(clientWhitelist);

            S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
            connection.sendTCP(welcomePacket);
        }
    }

    public void handleDisconnected(Connection connection) {
        if (connection.getClient() != null) {
            String hostAddress = connection.getClient().getIp();
            ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwid(hostAddress, connection.getHwid());
            if (clientWhitelist != null) {
                clientWhitelist.setIsActive(false);
                clientWhitelistService.save(clientWhitelist);
            }

            AntiCheatManager.getInstance().removeClient(connection.getClient());
            connection.setClient(null);
        }
    }
}
