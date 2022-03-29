package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChatRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChatAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

import java.util.List;
import java.util.stream.Collectors;

public class GuildChatRequestPacketHandler extends AbstractHandler {
    private C2SGuildChatRequestPacket guildChatRequestPacket;

    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;

    public GuildChatRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChatRequestPacket = new C2SGuildChatRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                .filter(x -> !x.getWaitingForApproval())
                .collect(Collectors.toList());
        List<Integer> allPlayerIds = guildMembers.stream()
                .map(x -> x.getPlayer().getId().intValue())
                .collect(Collectors.toList());
        List<Client> allClients = GameManager.getInstance().getClients().stream()
                .filter(c -> c.getActivePlayer() != null && allPlayerIds.contains(c.getActivePlayer().getId().intValue()))
                .collect(Collectors.toList());

        allClients.forEach(c -> {
            if (c.getConnection() != null && c.getConnection().isConnected()) {
                c.getConnection().sendTCP(new S2CGuildChatAnswerPacket(activePlayer.getName(), guildChatRequestPacket.getMessage()));
            }
        });
    }
}
