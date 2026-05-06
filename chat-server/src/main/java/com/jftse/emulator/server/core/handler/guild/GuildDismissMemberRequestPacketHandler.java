package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildDismissMember;
import com.jftse.server.core.shared.packets.guild.SMSGGuildDismissMember;

@PacketId(CMSGGuildDismissMember.PACKET_ID)
public class GuildDismissMemberRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildDismissMember> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildDismissMemberRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildDismissMember guildDismissMemberRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildService.findWithMembersById(guildMember.getGuild().getId());
            GuildMember dismissMember = GameManager.getInstance().getGuildMemberByPlayerPositionInGuild(
                    guild,
                    guildDismissMemberRequestPacket.getPlayerPositionInGuild());

            if (dismissMember != null) {
                if (dismissMember.getMemberRank() == 3) {
                    SMSGGuildDismissMember dismissMemberPacket = SMSGGuildDismissMember.builder().result((short) -5).build();
                    connection.sendTCP(dismissMemberPacket);
                } else {
                    guild.getMemberList().removeIf(x -> x.getId().equals(dismissMember.getId()));
                    guildService.save(guild);

                    FTClient targetClient = GameManager.getInstance().getClients().stream()
                            .filter(c -> c.hasPlayer() && dismissMember.getPlayer().getId().equals(c.getPlayer().getId()))
                            .findFirst()
                            .orElse(null);

                    SMSGGuildDismissMember dismissMemberPacket = SMSGGuildDismissMember.builder().result((short) 0).build();
                    if (targetClient != null) {
                        FTPlayer targetPlayer = targetClient.getPlayer();
                        targetPlayer.setGuildMemberId(null);
                        targetPlayer.setGuild(null);

                        targetClient.getConnection().sendTCP(dismissMemberPacket);
                    }
                    connection.sendTCP(dismissMemberPacket);
                }
            }
        }
    }
}
