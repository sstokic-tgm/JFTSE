package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.AuthenticationManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGDefault;
import com.jftse.server.core.shared.packets.CMSGDevPacket;

@PacketId(CMSGDevPacket.PACKET_ID)
public class DevPacketHandler implements PacketHandler<FTConnection, CMSGDevPacket> {
    @Override
    public void handle(FTConnection connection, CMSGDevPacket packet) {
        final boolean handleDevPackets = AuthenticationManager.getInstance().isHandleDevPackets();
        if (handleDevPackets) {
            CMSGDefault packetToRelay = CMSGDefault.fromBytes(packet.getPacket());
            AuthenticationManager.getInstance().getClients().forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(packetToRelay);
                }
            });
        }
    }
}
