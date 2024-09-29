package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildChatRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildChatAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;

import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SGuildChatRequest)
public class GuildChatRequestPacketHandler extends AbstractPacketHandler {
    private C2SGuildChatRequestPacket guildChatRequestPacket;

    private final GuildMemberService guildMemberService;

    public GuildChatRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChatRequestPacket = new C2SGuildChatRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .collect(Collectors.toList());
        List<Integer> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId().intValue())
                .collect(Collectors.toList());
        List<FTClient> allClients = GameManager.getInstance().getClients().stream()
                .filter(c -> c.getPlayer() != null && allPlayerIds.contains(c.getPlayer().getId().intValue()))
                .collect(Collectors.toList());

        allClients.forEach(c -> {
            if (c.getConnection() != null) {
                c.getConnection().sendTCP(new S2CGuildChatAnswerPacket(activePlayer.getName(), guildChatRequestPacket.getMessage()));
            }
        });
    }
}
