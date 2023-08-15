package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.packets.guild.C2SGuildDismissMemberRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDismissMemberAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;

@PacketOperationIdentifier(PacketOperations.C2SGuildDismissMemberRequest)
public class GuildDismissMemberRequestPacketHandler extends AbstractPacketHandler {
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
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
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

                    FTClient targetClient = GameManager.getInstance().getClients().stream()
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
