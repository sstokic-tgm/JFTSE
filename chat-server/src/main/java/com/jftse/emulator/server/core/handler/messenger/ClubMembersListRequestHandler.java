package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.rabbit.messages.GuildMemberListOnRequestMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.messenger.CMSGGuildMemberList;

@PacketId(CMSGGuildMemberList.PACKET_ID)
public class ClubMembersListRequestHandler implements PacketHandler<FTConnection, CMSGGuildMemberList> {
    private final RProducerService rProducerService;

    public ClubMembersListRequestHandler() {
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildMemberList packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        FTPlayer activePlayer = ftClient.getPlayer();

        GuildMemberListOnRequestMessage message = GuildMemberListOnRequestMessage.builder()
                .playerId(activePlayer.getId())
                .build();
        rProducerService.send(message, "game.messenger.guildList chat.messenger.guildList", "GameServer");
    }
}
