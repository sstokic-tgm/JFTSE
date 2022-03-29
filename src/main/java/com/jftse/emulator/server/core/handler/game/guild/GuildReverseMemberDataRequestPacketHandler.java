package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildReserveMemberDataRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildReverseMemberAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class GuildReverseMemberDataRequestPacketHandler extends AbstractHandler {
    private C2SGuildReserveMemberDataRequestPacket c2SGuildReserveMemberDataRequestPacket;

    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;

    public GuildReverseMemberDataRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SGuildReserveMemberDataRequestPacket = new C2SGuildReserveMemberDataRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (c2SGuildReserveMemberDataRequestPacket.getPage() != 0) return;
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null) return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> reverseMemberList = guildMember.getGuild().getMemberList().stream()
                .filter(GuildMember::getWaitingForApproval)
                .collect(Collectors.toList());
        connection.sendTCP(new S2CGuildReverseMemberAnswerPacket(reverseMemberList));
    }
}
