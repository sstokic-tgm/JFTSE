package com.jftse.emulator.server.core.handler;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.gameserver.GameServer;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.AuthenticationService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.auth.CMSGRequestChannelList;
import com.jftse.server.core.shared.packets.auth.SMSGChannelList;

import java.util.List;

@PacketId(CMSGRequestChannelList.PACKET_ID)
public class LoginAliveClientHandler implements PacketHandler<FTConnection, CMSGRequestChannelList> {
    private final AuthenticationService authenticationService;
    private final PlayerService playerService;

    public LoginAliveClientHandler() {
        authenticationService = ServiceManager.getInstance().getAuthenticationService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRequestChannelList packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        Account account = client.getAccount();
        if (account == null)
            return;

        if (client.isClientAlive().compareAndSet(false, true)) {
            List<GameServer> gameServerList = authenticationService.getGameServerList();
            SMSGChannelList channelList = SMSGChannelList.builder()
                    .channels(gameServerList.stream().map(gs -> com.jftse.server.core.shared.packets.auth.Channel.builder()
                            .id((byte) Math.toIntExact(gs.getId()))
                            .id2((short) Math.toIntExact(gs.getId()))
                            .type(gs.getGameServerType().getType())
                            .hostname(gs.getHost())
                            .port(gs.getPort().shortValue())
                            .population((short) 0)
                            .customChannel(gs.getIsCustomChannel())
                            .name(gs.getName())
                            .build()).toList()
                    ).build();
            connection.sendTCP(channelList);
        }
    }
}
