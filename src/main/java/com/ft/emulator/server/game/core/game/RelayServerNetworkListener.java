package com.ft.emulator.server.game.core.game;

import com.ft.emulator.server.game.core.game.handler.MatchplayPacketHandler;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.ConnectionListener;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Log4j2
public class RelayServerNetworkListener implements ConnectionListener {
    @Autowired
    private MatchplayPacketHandler matchplayPacketHandler;

    public void connected(Connection connection) {
        long timeout = TimeUnit.MINUTES.toMillis(5);
        connection.getTcpConnection().setTimeoutMillis((int) timeout);

        matchplayPacketHandler.sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {
        matchplayPacketHandler.handleDisconnected(connection);
    }

    public void received(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
            case PacketID.C2SRelayPacketToAllClients:
                matchplayPacketHandler.handleRelayPacketToClientsInGameSessionRequest(connection, packet);
                break;

            case PacketID.C2SMatchplayRegisterPlayerForGameSession:
                matchplayPacketHandler.handleRegisterPlayerForSession(connection, packet);
                break;

            case PacketID.C2SHeartbeat:
            case PacketID.C2SLoginAliveClient:
                // empty..
                break;

            default:
                // empty
                break;
        }
    }

    public void idle(Connection connection) {
        // empty..
    }

    public void onException(Connection connection, Exception exception) {
        log.error(exception.getMessage(), exception);
    }
}
