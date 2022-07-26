package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildReserveMemberDataRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildReverseMemberAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class GuildReverseMemberDataRequestPacketHandler extends AbstractHandler {
    private C2SGuildReserveMemberDataRequestPacket c2SGuildReserveMemberDataRequestPacket;

    private final GuildMemberService guildMemberService;

    public GuildReverseMemberDataRequestPacketHandler() {
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
        if (connection.getClient() == null || connection.getClient().getPlayer() == null) return;

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> reverseMemberList = guildMember.getGuild().getMemberList().stream()
                .filter(GuildMember::getWaitingForApproval)
                .collect(Collectors.toList());
        connection.sendTCP(new S2CGuildReverseMemberAnswerPacket(reverseMemberList));
    }
}
