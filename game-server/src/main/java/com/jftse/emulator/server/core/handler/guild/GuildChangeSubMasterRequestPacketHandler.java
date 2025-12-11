package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildChangeSubMaster;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChangeSubMaster;

@PacketId(CMSGGuildChangeSubMaster.PACKET_ID)
public class GuildChangeSubMasterRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildChangeSubMaster> {
    private final GuildMemberService guildMemberService;

    public GuildChangeSubMasterRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChangeSubMaster guildChangeSubMasterRequestPacket) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null) {
            connection.sendTCP(SMSGGuildChangeSubMaster.builder().status((byte) 0).result((short) -1).build());
            return;
        }

        Player activePlayer = client.getPlayer();
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
                        connection.sendTCP(SMSGGuildChangeSubMaster.builder().status((byte) 0).result((short) -5).build());
                    } else {
                        subClubMaster.setMemberRank((byte) 2);
                        guildMemberService.save((subClubMaster));
                        connection.sendTCP(SMSGGuildChangeSubMaster.builder().status((byte) 1).result((short) 0).build());
                    }
                } else {
                    subClubMaster.setMemberRank((byte) 1);
                    guildMemberService.save((subClubMaster));
                    connection.sendTCP(SMSGGuildChangeSubMaster.builder().status((byte) 0).result((short) 0).build());
                }
            }
        } else {
            connection.sendTCP(SMSGGuildChangeSubMaster.builder().status((byte) 0).result((short) -1).build());
        }
    }
}
