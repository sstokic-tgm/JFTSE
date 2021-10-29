package com.jftse.emulator.server.core.handler.relay;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.concurrent.ConcurrentLinkedDeque;

public class RelayPacketRequestHandler extends AbstractHandler {
    private Packet relayPacket;

    @Override
    public boolean process(Packet packet) {
        relayPacket = new Packet(packet.getData());
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() != null) {
            GameSession gameSession = connection.getClient().getActiveGameSession();
            if (gameSession != null) {
                ConcurrentLinkedDeque<Client> clientList = RelayManager.getInstance().getClientsInGameSession(gameSession.getSessionId());
                for (Client client : clientList) {
                    if (client.getConnection() != null && client.getConnection().isConnected()) {
                        client.getConnection().sendTCP(relayPacket);
                    }
                }
            }
        }
    }
}
