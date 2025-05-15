package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.rabbit.messages.GuildMemberListOnRequestMessage;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SClubMembersListRequest)
public class ClubMembersListRequestHandler extends AbstractPacketHandler {
    private final RProducerService rProducerService;

    public ClubMembersListRequestHandler() {
        rProducerService = RProducerService.getInstance();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Player activePlayer = ftClient.getPlayer();

        GuildMemberListOnRequestMessage message = GuildMemberListOnRequestMessage.builder()
                .playerId(activePlayer.getId())
                .build();
        rProducerService.send(message, "game.messenger.guildList chat.messenger.guildList", "GameServer");
    }
}
