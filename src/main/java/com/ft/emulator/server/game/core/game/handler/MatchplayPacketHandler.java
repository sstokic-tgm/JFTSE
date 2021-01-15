package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.game.core.packet.packets.S2CWelcomePacket;
import com.ft.emulator.server.networking.Connection;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.RelayHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchplayPacketHandler {
    private final RelayHandler relayHandler;

    public RelayHandler getRelayHandler() {
        return relayHandler;
    }

    public void sendWelcomePacket(Connection connection) {
        S2CWelcomePacket welcomePacket = new S2CWelcomePacket(0, 0, 0, 0);
        connection.sendTCP(welcomePacket);
    }

    public void handleRelayPacketToAllClientsRequest(Connection connection, Packet packet) {
        List<Client> clientList = this.getRelayHandler().getClientList();
        Packet relayPacket = new Packet(packet.getData());
        for (Client client : clientList) {
            if (client.getConnection().getId() == connection.getId()) {
                continue;
            }

            client.getConnection().sendTCP(relayPacket);
        }
    }

    public void handlePlayerInformationPacket(Connection connection, Packet packet) {
        Packet answer = new Packet(PacketID.S2CMatchplayAckPlayerInformation);
        answer.write((byte)0);
        connection.sendTCP(answer);
    }

    public void handleUnknown(Connection connection, Packet packet) {
        Packet unknownAnswer = new Packet((char) (packet.getPacketId() + 1));
        if (unknownAnswer.getPacketId() == (char) 0x200E) {
            unknownAnswer.write((char) 1);
        }
        else {
            unknownAnswer.write((short) 0);
        }
        connection.sendTCP(unknownAnswer);
    }
}
