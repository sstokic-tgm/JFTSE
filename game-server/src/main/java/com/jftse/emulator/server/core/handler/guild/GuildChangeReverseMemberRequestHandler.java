package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
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
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            GuildMember reverseMember = guildMember.getGuild().getMemberList().stream()
                    .filter(gm -> gm.getPlayer().getId() == guildChangeReverseMemberRequestPacket.getPlayerId())
                    .findFirst().orElse(null);

            if (reverseMember != null) {
                if (guildChangeReverseMemberRequestPacket.getStatus() == 1) {
                    reverseMember.setWaitingForApproval(false);
                    guildMemberService.save(reverseMember);

                    connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 1).result((short) 0).build());
                } else {
                    reverseMember.getGuild().getMemberList().removeIf(x -> x.getId().equals(reverseMember.getId()));
                    guildService.save(reverseMember.getGuild());

                    connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 0).result((short) 0).build());
                }
            }
        } else {
            connection.sendTCP(SMSGGuildChangeReverseMember.builder().status((byte) 0).result((short) -4).build());
        }
    }
}
