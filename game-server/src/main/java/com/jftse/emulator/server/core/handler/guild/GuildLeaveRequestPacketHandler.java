package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildLeave;
import com.jftse.server.core.shared.packets.guild.SMSGGuildLeave;

@PacketId(CMSGGuildLeave.PACKET_ID)
public class GuildLeaveRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildLeave> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildLeaveRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildLeave packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());
        if (guildMember.getMemberRank() == 3) {
            SMSGGuildLeave guildLeave = SMSGGuildLeave.builder().status((char) -2).build();
            connection.sendTCP(guildLeave);
        } else {
            Guild guild = guildService.findWithMembersById(guildMember.getGuild().getId());
            guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
            guildService.save(guild);

            activePlayer.setGuildMemberId(null);
            activePlayer.setGuild(null);

            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, guild));
        }
    }
}
