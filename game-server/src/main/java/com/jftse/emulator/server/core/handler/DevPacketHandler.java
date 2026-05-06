package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.CMSGDefault;
import com.jftse.server.core.shared.packets.CMSGDevPacket;

@PacketId(CMSGDevPacket.PACKET_ID)
public class DevPacketHandler implements PacketHandler<FTConnection, CMSGDevPacket> {
    private final ConfigService configService;

    public DevPacketHandler() {
        configService = ServiceManager.getInstance().getConfigService();
    }

    @Override
    public void handle(FTConnection connection, CMSGDevPacket packet) {
        if (configService.getValue("dev.packets.handle", false)) {
            CMSGDefault packetToRelay = CMSGDefault.fromBytes(packet.getPacket());
            GameManager.getInstance().getClients().forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(packetToRelay);
                }
            });
        }
    }
}
