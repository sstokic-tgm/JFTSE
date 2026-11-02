package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerMovementSnapshot;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerMovementSnapshot;
import com.jftse.server.core.shared.packets.translator.PlayerMovementSnapshotTranslator;

import java.util.List;

@PacketId(CMSGPlayerMovementSnapshot.PACKET_ID)
public class PlayerMovementSnapshotHandler implements PacketHandler<FTConnection, CMSGPlayerMovementSnapshot> {
    private static final IPacketTranslator<SMSGPlayerMovementSnapshot, CMSGPlayerMovementSnapshot> translator = new PlayerMovementSnapshotTranslator();

    @Override
    public void handle(FTConnection connection, CMSGPlayerMovementSnapshot packet) {
        SMSGPlayerMovementSnapshot relayPacket = packet.translate(translator);
        connection.getClient().getGameSessionId().ifPresent(gameSessionId -> {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(gameSessionId);
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        });
    }
}
