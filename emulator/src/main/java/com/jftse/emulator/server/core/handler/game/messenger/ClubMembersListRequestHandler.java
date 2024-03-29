package com.jftse.emulator.server.core.handler.game.messenger;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ClubMembersListRequestHandler extends AbstractHandler {
    private final GuildMemberService guildMemberService;

    public ClubMembersListRequestHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer =connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null) {
            Guild guild = guildMember.getGuild();

            if (guild != null) {
                List<GuildMember> guildMembers = guild.getMemberList().stream()
                        .filter(gm -> !gm.getId().equals(guildMember.getId()))
                        .sorted(Comparator.comparing(GuildMember::getMemberRank).reversed())
                        .collect(Collectors.toList());

                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                connection.sendTCP(s2CClubMembersListAnswerPacket);
            }
        }
    }
}
