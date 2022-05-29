package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildDismissMemberRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDismissMemberAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;
import com.jftse.emulator.server.shared.module.Client;

public class GuildDismissMemberRequestPacketHandler extends AbstractHandler {
    private C2SGuildDismissMemberRequestPacket guildDismissMemberRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildDismissMemberRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildDismissMemberRequestPacket = new C2SGuildDismissMemberRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            GuildMember dismissMember = GameManager.getInstance().getGuildMemberByPlayerPositionInGuild(
                    guildDismissMemberRequestPacket.getPlayerPositionInGuild(),
                    guildMember);

            if (dismissMember != null) {
                if (dismissMember.getMemberRank() == 3) {
                    S2CGuildDismissMemberAnswerPacket answerPacketForDismissedMember = new S2CGuildDismissMemberAnswerPacket((short) -5);
                    connection.sendTCP(answerPacketForDismissedMember);
                } else {
                    Guild guild = dismissMember.getGuild();
                    guild.getMemberList().removeIf(x -> x.getId().equals(dismissMember.getId()));
                    guildService.save(guild);

                    Client targetClient = GameManager.getInstance().getClients().stream()
                            .filter(c -> c.getPlayer() != null && c.getPlayer().getId().equals(dismissMember.getPlayer().getId()))
                            .findFirst()
                            .orElse(null);

                    if (targetClient != null) {
                        S2CGuildDismissMemberAnswerPacket answerPacketForDismissedMember = new S2CGuildDismissMemberAnswerPacket((short) 0);
                        targetClient.getConnection().sendTCP(answerPacketForDismissedMember);
                    }
                }
            }
        }
    }
}
