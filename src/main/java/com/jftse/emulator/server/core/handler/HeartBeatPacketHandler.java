package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.common.service.ConfigService;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.ClientWhitelistService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.anticheat.ClientWhitelist;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class HeartBeatPacketHandler extends AbstractHandler {
    private final PlayerService playerService;
    private final ClientWhitelistService clientWhitelistService;

    private final ConfigService configService;

    public HeartBeatPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        clientWhitelistService = ServiceManager.getInstance().getClientWhitelistService();
        configService = ServiceManager.getInstance().getConfigService();
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

        if (!configService.getValue("anticheat.enabled", false) || connection.getClient() == null || connection.getClient().getAccount() == null)
            return;

        String hostAddress = connection.getClient().getIp();
        ClientWhitelist clientWhitelist = clientWhitelistService.findByIpAndHwidAndAccount(hostAddress, connection.getHwid(), connection.getClient().getAccount());
        if (clientWhitelist != null && !clientWhitelist.getIsActive())
            connection.close();
    }
}
