package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildChatMessage;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChatMessage;

import java.util.List;

@PacketId(CMSGGuildChatMessage.PACKET_ID)
public class GuildChatRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildChatMessage> {
    private final GuildMemberService guildMemberService;

    public GuildChatRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChatMessage guildChatRequestPacket) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .toList();
        List<Integer> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId().intValue())
                .toList();
        List<FTClient> allClients = GameManager.getInstance().getClients().stream()
                .filter(c -> c.getPlayer() != null && allPlayerIds.contains(c.getPlayer().getId().intValue()))
                .toList();

        allClients.forEach(c -> {
            if (c.getConnection() != null) {
                c.getConnection().sendTCP(SMSGGuildChatMessage.builder().sender(activePlayer.getName()).message(guildChatRequestPacket.getMessage()).build());
            }
        });
    }
}
