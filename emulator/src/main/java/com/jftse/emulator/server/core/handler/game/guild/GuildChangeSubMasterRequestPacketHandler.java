package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChangeSubMasterRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChangeSubMasterAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildChangeSubMasterRequestPacketHandler extends AbstractHandler {
    private C2SGuildChangeSubMasterRequestPacket guildChangeSubMasterRequestPacket;

    private final GuildMemberService guildMemberService;

    public GuildChangeSubMasterRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeSubMasterRequestPacket = new C2SGuildChangeSubMasterRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null) {
            connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) -1));
            return;
        }

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            GuildMember subClubMaster = GameManager.getInstance().getGuildMemberByPlayerPositionInGuild(
                    guildChangeSubMasterRequestPacket.getPlayerPositionInGuild(),
                    guildMember);

            if (subClubMaster != null) {
                Guild guild = subClubMaster.getGuild();
                long subMasterCount = guild.getMemberList().stream()
                        .filter(x -> !x.getWaitingForApproval() && x.getMemberRank() == 2)
                        .count();

                if (guildChangeSubMasterRequestPacket.getStatus() == 1) {
                    if (subMasterCount == 3) {
                        connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) -5));
                    } else {
                        subClubMaster.setMemberRank((byte) 2);
                        guildMemberService.save((subClubMaster));
                        connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 1, (short) 0));
                    }
                } else {
                    subClubMaster.setMemberRank((byte) 1);
                    guildMemberService.save((subClubMaster));
                    connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) 0));
                }
            }
        } else {
            connection.sendTCP(new S2CGuildChangeSubMasterAnswerPacket((byte) 0, (short) -1));
        }
    }
}
