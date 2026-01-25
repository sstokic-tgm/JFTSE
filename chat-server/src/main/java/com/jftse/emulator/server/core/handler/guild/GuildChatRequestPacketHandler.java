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
import com.jftse.server.core.shared.packets.guild.CMSGGuildChatMessage;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChatMessage;

import java.util.List;

@PacketId(CMSGGuildChatMessage.PACKET_ID)
public class GuildChatRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildChatMessage> {
    private final GuildMemberService guildMemberService;
    private final GuildService guildService;

    public GuildChatRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChatMessage guildChatRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());
        if (guildMember == null) {
            return;
        }

        Guild guild = guildService.findWithMembersById(guildMember.getGuild().getId());

        List<GuildMember> guildMembers = guild.getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .toList();
        List<Long> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId())
                .toList();
        List<FTClient> allClients = GameManager.getInstance().getClients().stream()
                .filter(c -> c.hasPlayer() && allPlayerIds.contains(c.getPlayer().getId()))
                .toList();

        allClients.forEach(c -> {
            if (c.getConnection() != null) {
                c.getConnection().sendTCP(SMSGGuildChatMessage.builder().sender(activePlayer.getName()).message(guildChatRequestPacket.getMessage()).build());
            }
        });
    }
}
