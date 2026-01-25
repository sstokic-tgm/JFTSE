package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildChangeReverseMember;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChangeReverseMember;

@PacketId(CMSGGuildChangeReverseMember.PACKET_ID)
public class GuildChangeReverseMemberRequestHandler implements PacketHandler<FTConnection, CMSGGuildChangeReverseMember> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildChangeReverseMemberRequestHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChangeReverseMember guildChangeReverseMemberRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());

        boolean isApproved = guildChangeReverseMemberRequestPacket.getStatus() == 1;

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            guildService.changeReverseMemberStatus(
                    guildMember.getGuild().getId(),
                    guildChangeReverseMemberRequestPacket.getPlayerId(),
                    isApproved
            );

            if (isApproved) {
                connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 1).result((short) 0).build());
            } else {
                connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 0).result((short) 0).build());
            }
        } else {
            connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 0).result((short) -4).build());
        }
    }
}
