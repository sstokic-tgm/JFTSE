package com.jftse.emulator.server.game.core.anticheat;

import com.jftse.emulator.server.game.core.anticheat.handler.AntiCheatPacketHandler;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class AntiCheatHeartBeatNetworkListener implements ConnectionListener {
    @Autowired
    private AntiCheatPacketHandler antiCheatPacketHandler;

    public void connected(Connection connection) {
        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        antiCheatPacketHandler.getAntiCheatHandler().addClient(client);
        antiCheatPacketHandler.sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {
        antiCheatPacketHandler.handleDisconnected(connection);
    }

    public void received(Connection connection, Packet packet) {

        switch (packet.getPacketId()) {

            case 0x9791:
                antiCheatPacketHandler.handleRegister(connection, packet);
                break;

            default:
                antiCheatPacketHandler.handleUnknown(connection, packet);
        }
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        switch ("" + exception.getMessage()) {
            case "Connection is closed.":
            case "Connection reset by peer":
            case "Broken pipe":
                break;
            default:
                log.error(exception.getMessage(), exception);
        }
    }

    public void onTimeout(Connection connection) {
        connection.close();
    }

    public void cleanUp() {
        antiCheatPacketHandler.handleCleanUp();
    }
}