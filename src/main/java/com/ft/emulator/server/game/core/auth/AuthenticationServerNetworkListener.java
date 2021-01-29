package com.ft.emulator.server.game.core.auth;

import com.ft.emulator.server.game.core.auth.handler.AuthPacketHandler;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.ConnectionListener;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class AuthenticationServerNetworkListener implements ConnectionListener {
    @Autowired
    private AuthPacketHandler authPacketHandler;

    public void connected(Connection connection) {
        Client client = new Client();
        client.setConnection(connection);

        connection.setClient(client);
        authPacketHandler.sendWelcomePacket(connection);
    }

    public void disconnected(Connection connection) {
        authPacketHandler.handleDisconnected(connection);
    }

    public void received(Connection connection, Packet packet) {
        switch (packet.getPacketId()) {
        case PacketID.C2SLoginRequest:
            authPacketHandler.handleLoginPacket(connection, packet);
            break;

        case PacketID.C2SDisconnectRequest:
            authPacketHandler.handleDisconnectPacket(connection, packet);
            break;

        case PacketID.C2SLoginFirstPlayerRequest:
            authPacketHandler.handleFirstPlayerPacket(connection, packet);
            break;

        case PacketID.C2SPlayerNameCheck:
            authPacketHandler.handlePlayerNameCheckPacket(connection, packet);
            break;

        case PacketID.C2SPlayerCreate:
            authPacketHandler.handlePlayerCreatePacket(connection, packet);
            break;

        case PacketID.C2SPlayerDelete:
            authPacketHandler.handlePlayerDeletePacket(connection, packet);
            break;

        case PacketID.C2SAuthLoginData:
            authPacketHandler.handleAuthServerLoginPacket(connection, packet);
            break;

        case PacketID.C2SHeartbeat:
        case PacketID.C2SLoginAliveClient:
            break;

        default:
            authPacketHandler.handleUnknown(connection, packet);
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
