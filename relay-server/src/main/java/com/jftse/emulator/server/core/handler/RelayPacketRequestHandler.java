package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.utilities.BitKit;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.IPacket;
import com.jftse.server.core.protocol.PacketRegistry;
import com.jftse.server.core.shared.packets.CMSGDefault;
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
            if (relayPacket instanceof CMSGDefault defaultPacket) {
                // not found so its wrapped in CMSGDefault and prob not reversed yet
                // we must still relay it because the client expects the packet to function properly
                connection.sendTCP(defaultPacket);
            } else {
                // just queue the packet for processing since we know about it
                connection.queuePacket(relayPacket);
            }
        }
    }
}
