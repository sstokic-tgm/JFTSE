package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildReverseMemberAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildReverseMemberData;

import java.util.List;
import java.util.stream.Collectors;

@PacketId(CMSGGuildReverseMemberData.PACKET_ID)
public class GuildReverseMemberDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildReverseMemberData> {
    private final GuildMemberService guildMemberService;

    public GuildReverseMemberDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildReverseMemberData packet) {
        if (packet.getPage() != 0) return;
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null) return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> reverseMemberList = guildMember.getGuild().getMemberList().stream()
                .filter(GuildMember::getWaitingForApproval)
                .collect(Collectors.toList());
        connection.sendTCP(new S2CGuildReverseMemberAnswerPacket(reverseMemberList));
    }
}
