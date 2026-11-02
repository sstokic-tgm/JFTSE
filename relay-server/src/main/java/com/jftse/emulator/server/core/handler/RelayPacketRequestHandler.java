package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.RelayManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketFrames;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.packets.CMSGDefault;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerMovementSnapshot;
import com.jftse.server.core.shared.packets.relay.CMSGRelay;
import com.jftse.server.core.shared.packets.translator.PlayerMovementStateTranslator;

import java.util.List;

@PacketId(CMSGRelay.PACKET_ID)
public class RelayPacketRequestHandler implements PacketHandler<FTConnection, CMSGRelay> {
    private static final PlayerMovementStateTranslator movementStateTranslator = new PlayerMovementStateTranslator();
    @Override
    public void handle(FTConnection connection, CMSGRelay relay) {
        FTClient client = connection.getClient();
        if (client == null || client.getGameSessionId().isEmpty())
            return;

        byte[] innerPacket = relay.getPacket();
        PacketFrames.validate(innerPacket);
        Packet frame = new Packet(innerPacket);
        if (innerPacket.length != 8 + frame.getDataLength())
            throw new IllegalArgumentException("Expected one inner relay frame");

        int packetId = frame.getPacketId();
        boolean alternateMovement = packetId == CMSGPlayerMovementSnapshot.PACKET_ID && frame.getDataLength() == PlayerMovementStateTranslator.PAYLOAD_LENGTH;
        // Schema-only definitions must not replace working raw forwarding with a dropped queue entry.
        IPacket relayPacket;
        if (alternateMovement) {
            relayPacket = movementStateTranslator.translate(frame);
        } else if (PacketRegistry.getHandler(packetId) == null) {
            relayPacket = frame;
        } else {
            relayPacket = PacketRegistry.decode(packetId, innerPacket);
        }
        if (alternateMovement || relayPacket == frame || relayPacket instanceof CMSGDefault) {
            final List<FTClient> clients = RelayManager.getInstance().getClientsInSession(client.getGameSessionId().get());
            clients.forEach(c -> {
                if (c.getConnection() != null)
                    c.getConnection().sendTCP(relayPacket);
            });
        } else {
            connection.queuePacket(relayPacket);
        }
    }
}
