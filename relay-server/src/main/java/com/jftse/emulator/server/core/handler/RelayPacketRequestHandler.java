package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SRelayPacketToAllClients)
@Log4j2
public class RelayPacketRequestHandler extends AbstractPacketHandler {
    private Packet relayPacket;

    @Override
    public boolean process(Packet packet) {
        relayPacket = new Packet(packet.getData());
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client != null) {
            if (client.getGameSessionId().isPresent()) {
                final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(client.getGameSessionId().get());
                clients.forEach(c -> {
                    if (c.getConnection() != null)
                        c.getConnection().sendTCP(relayPacket);
                });
            }
        }
    }
}
