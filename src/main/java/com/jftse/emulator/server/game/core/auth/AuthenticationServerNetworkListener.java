package com.jftse.emulator.server.game.core.auth;

import com.jftse.emulator.server.game.core.auth.handler.AuthPacketHandler;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.Connection;
import com.jftse.emulator.server.networking.ConnectionListener;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

@Log4j2
public class AuthenticationServerNetworkListener implements ConnectionListener {
    @Autowired
    private AuthPacketHandler authPacketHandler;

    public void cleanUp() {
        // empty..
    }

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
        case 0xE00E:
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
}
