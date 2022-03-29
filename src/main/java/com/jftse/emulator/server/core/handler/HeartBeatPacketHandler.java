package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class HeartBeatPacketHandler extends AbstractHandler {
    private final PlayerService playerService;

    public HeartBeatPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() != null && connection.getClient().getActivePlayer() != null) {
            Player player = playerService.findById(connection.getClient().getActivePlayer().getId());
            if (player.getAccount().getStatus() == -6)
                connection.close();
        }
    }
}
