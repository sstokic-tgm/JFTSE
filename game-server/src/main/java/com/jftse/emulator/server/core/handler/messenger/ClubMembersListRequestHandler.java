package com.jftse.emulator.server.core.handler.messenger;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.messenger.S2CClubMembersListAnswerPacket;
import com.jftse.emulator.server.core.rabbit.service.RProducerService;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.SocialService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SClubMembersListRequest)
public class ClubMembersListRequestHandler extends AbstractPacketHandler {
    private final GuildMemberService guildMemberService;
    private final SocialService socialService;

    private final RProducerService rProducerService;

    public ClubMembersListRequestHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        socialService = ServiceManager.getInstance().getSocialService();
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
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null) {
            Guild guild = guildMember.getGuild();

            if (guild != null) {
                List<GuildMember> guildMembers = guild.getMemberList().stream()
                        .filter(gm -> !gm.getId().equals(guildMember.getId()) && !gm.getWaitingForApproval())
                        .sorted(Comparator.comparing(GuildMember::getMemberRank).reversed())
                        .collect(Collectors.toList());

                S2CClubMembersListAnswerPacket s2CClubMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(guildMembers);
                connection.sendTCP(s2CClubMembersListAnswerPacket);

                guildMembers.forEach(gm -> {
                    List<GuildMember> otherGuildMembers = socialService.getGuildMemberList(gm.getPlayer());

                    S2CClubMembersListAnswerPacket otherGuildMembersListAnswerPacket = new S2CClubMembersListAnswerPacket(otherGuildMembers);
                    FTConnection guildMemberConnection = GameManager.getInstance().getConnectionByPlayerId(gm.getPlayer().getId());
                    if (guildMemberConnection != null) {
                        guildMemberConnection.sendTCP(otherGuildMembersListAnswerPacket);
                    } else {
                        rProducerService.send("playerId", gm.getPlayer().getId(), otherGuildMembersListAnswerPacket);
                    }
                });
            }
        }
    }
}
