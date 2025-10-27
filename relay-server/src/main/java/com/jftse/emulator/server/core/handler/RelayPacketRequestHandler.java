package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.packets.relay.CMSGRelay;

@PacketId(CMSGRelay.PACKET_ID)
public class RelayPacketRequestHandler implements PacketHandler<FTConnection, CMSGRelay> {
    @Override
    public void handle(FTConnection connection, CMSGRelay relay) {
        FTClient client = connection.getClient();
        if (client.getGameSessionId().isPresent()) {
            byte[] innerPacket = relay.getPacket();
            int packetId = BitKit.bytesToShort(innerPacket, 4);
            IPacket relayPacket = PacketRegistry.decode(packetId, innerPacket);
            connection.queuePacket(relayPacket);
        }
    }
}
