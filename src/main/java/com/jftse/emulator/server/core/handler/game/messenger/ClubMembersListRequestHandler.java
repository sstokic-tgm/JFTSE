package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class ClubMembersListRequestHandler extends AbstractHandler {
    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;

    public ClubMembersListRequestHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null) {
            Guild guild = guildMember.getGuild();

            if (guild != null) {
                List<GuildMember> guildMembers = guild.getMemberList().stream()
                        .filter(gm -> !gm.getId().equals(guildMember.getId()))
                        .collect(Collectors.toList());

                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                connection.sendTCP(s2CClubMembersListAnswerPacket);
            }
        }
    }
}
