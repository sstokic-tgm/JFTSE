package com.ft.emulator.server.game.core.game.handler;

import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;
import com.ft.emulator.server.shared.module.GameHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MatchplayPacketHandler {
    private final GameHandler gameHandler;

    public GameHandler getGameHandler() {
        return gameHandler;
    }

    public void handleRelayPacketToAllClientsRequest(Packet packet) {
        List<Client> clientList = this.gameHandler.getClientList();
        Packet relayPacket = new Packet(packet.getData());
        for (Client client : clientList) {
            client.getConnection().sendTCP(relayPacket);
        }
    }
}
