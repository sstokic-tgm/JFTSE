package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildMemberDataRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildMemberDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SGuildMemberDataRequest)
public class GuildMemberDataRequestPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
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
