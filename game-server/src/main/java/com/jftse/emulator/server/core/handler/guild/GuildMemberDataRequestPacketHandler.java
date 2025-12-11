package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildMemberDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildMemberData;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PacketId(CMSGGuildMemberData.PACKET_ID)
public class GuildMemberDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildMemberData> {
    private final GuildMemberService guildMemberService;

    public GuildMemberDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildMemberData packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && packet.getPage() == 0) {
            List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                    .filter(x -> !x.getWaitingForApproval())
                    .sorted(Comparator.comparingInt(GuildMember::getMemberRank).reversed())
                    .collect(Collectors.toList());

            connection.sendTCP(new S2CGuildMemberDataAnswerPacket(guildMembers));
        }
    }
}
