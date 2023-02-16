package com.jftse.emulator.server.core.handler.game;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.networking.packet.Packet;

public class DevPacketHandler extends AbstractHandler {
    private Packet packet;

    private final ConfigService configService;

    public DevPacketHandler() {
        configService = ServiceManager.getInstance().getConfigService();
    }

    @Override
    public boolean process(Packet packet) {
        this.packet = packet;
        return true;
    }

    @Override
    public void handle() {
        if (configService.getValue("dev.packets.handle", false)) {
            byte[] data = packet.getData();
            Packet packetToRelay = new Packet(data);
            GameManager.getInstance().getClients().forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(packetToRelay);
                }
            });
        }
    }
}
