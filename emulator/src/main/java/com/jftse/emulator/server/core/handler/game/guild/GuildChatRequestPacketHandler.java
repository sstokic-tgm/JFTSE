package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChatRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChatAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;
import java.util.stream.Collectors;

public class GuildChatRequestPacketHandler extends AbstractHandler {
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
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .collect(Collectors.toList());
        List<Integer> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId().intValue())
                .collect(Collectors.toList());
        List<Client> allClients = GameManager.getInstance().getClients().stream()
                .filter(c -> c.getPlayer() != null && allPlayerIds.contains(c.getPlayer().getId().intValue()))
                .collect(Collectors.toList());

        allClients.forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(new S2CGuildChatAnswerPacket(activePlayer.getName(), guildChatRequestPacket.getMessage()));
            }
        });
    }
}
