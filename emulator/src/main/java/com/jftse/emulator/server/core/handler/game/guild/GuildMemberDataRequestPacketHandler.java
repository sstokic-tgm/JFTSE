package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildMemberDataRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildMemberDataAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class GuildMemberDataRequestPacketHandler extends AbstractHandler {
    private C2SGuildMemberDataRequestPacket c2SGuildMemberDataRequestPacket;

    private final GuildMemberService guildMemberService;

    public GuildMemberDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SGuildMemberDataRequestPacket = new C2SGuildMemberDataRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && c2SGuildMemberDataRequestPacket.getPage() == 0) {
            List<GuildMember> guildMembers = guildMember.getGuild().getMemberList().stream()
                    .filter(x -> !x.getWaitingForApproval())
                    .sorted(Comparator.comparingInt(GuildMember::getMemberRank).reversed())
                    .collect(Collectors.toList());

            connection.sendTCP(new S2CGuildMemberDataAnswerPacket(guildMembers));
        }
    }
}
