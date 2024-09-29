package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildReserveMemberDataRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildReverseMemberAnswerPacket;
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

@PacketOperationIdentifier(PacketOperations.C2SGuildReserveMemberDataRequest)
public class GuildReverseMemberDataRequestPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null) return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        List<GuildMember> reverseMemberList = guildMember.getGuild().getMemberList().stream()
                .filter(GuildMember::getWaitingForApproval)
                .collect(Collectors.toList());
        connection.sendTCP(new S2CGuildReverseMemberAnswerPacket(reverseMemberList));
    }
}
