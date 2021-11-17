package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.server.core.packet.packets.S2CWelcomePacket;
import com.jftse.emulator.server.networking.Connection;

public class BasicGameHandler {
    public BasicGameHandler() {

    }

    public void sendWelcomePacket(Connection connection) {
        if (connection.getRemoteAddressTCP() != null) {
            String hostAddress = connection.getRemoteAddressTCP().getAddress().getHostAddress();
            int port = connection.getRemoteAddressTCP().getPort();

            connection.getClient().setIp(hostAddress);
            connection.getClient().setPort(port);

            S2CWelcomePacket welcomePacket = new S2CWelcomePacket(connection.getDecKey(), connection.getEncKey(), 0, 0);
            connection.sendTCP(welcomePacket);
        }
    }
}
