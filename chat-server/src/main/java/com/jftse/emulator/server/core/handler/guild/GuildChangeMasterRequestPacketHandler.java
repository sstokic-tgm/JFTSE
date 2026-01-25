package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildChangeMaster;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChangeMaster;

@PacketId(CMSGGuildChangeMaster.PACKET_ID)
public class GuildChangeMasterRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildChangeMaster> {
    private final GuildMemberService guildMemberService;

    public GuildChangeMasterRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChangeMaster guildChangeMasterRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            connection.sendTCP(SMSGGuildChangeMaster.builder().result((short) -1).build());
            return;
        }

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            GuildMember newClubMaster = GameManager.getInstance().getGuildMemberByPlayerPositionInGuild(
                    guildChangeMasterRequestPacket.getPlayerPositionInGuild(),
                    guildMember);

            if (newClubMaster != null) {
                guildMember.setMemberRank((byte) 2);
                guildMemberService.save(guildMember);

                newClubMaster.setMemberRank((byte) 3);
                guildMemberService.save(newClubMaster);

                connection.sendTCP(SMSGGuildChangeMaster.builder().result((short) 0).build());
            }
        } else {
            connection.sendTCP(SMSGGuildChangeMaster.builder().result((short) -1).build());
        }
    }
}
