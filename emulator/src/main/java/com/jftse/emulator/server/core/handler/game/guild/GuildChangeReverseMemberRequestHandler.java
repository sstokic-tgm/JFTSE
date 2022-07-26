package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChangeReverseMemberRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChangeReverseMemberAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildChangeReverseMemberRequestHandler extends AbstractHandler {
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
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
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
