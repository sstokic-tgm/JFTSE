package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.packets.guild.C2SGuildChangeReverseMemberRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildChangeReverseMemberAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;

@PacketOperationIdentifier(PacketOperations.C2SGuildChangeReverseMemberRequest)
public class GuildChangeReverseMemberRequestHandler extends AbstractPacketHandler {
    private C2SGuildChangeReverseMemberRequestPacket guildChangeReverseMemberRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildChangeReverseMemberRequestHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeReverseMemberRequestPacket = new C2SGuildChangeReverseMemberRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            GuildMember reverseMember = guildMember.getGuild().getMemberList().stream()
                    .filter(gm -> gm.getPlayer().getId() == guildChangeReverseMemberRequestPacket.getPlayerId())
                    .findFirst().orElse(null);

            if (reverseMember != null) {
                if (guildChangeReverseMemberRequestPacket.getStatus() == 1) {
                    reverseMember.setWaitingForApproval(false);
                    guildMemberService.save(reverseMember);

                    connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 1, (short) 0));
                } else {
                    reverseMember.getGuild().getMemberList().removeIf(x -> x.getId().equals(reverseMember.getId()));
                    guildService.save(reverseMember.getGuild());

                    connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 0, (short) 0));
                }
            }
        } else {
            connection.sendTCP(new S2CGuildChangeReverseMemberAnswerPacket((byte) 0, (short) -4));
        }
    }
}
