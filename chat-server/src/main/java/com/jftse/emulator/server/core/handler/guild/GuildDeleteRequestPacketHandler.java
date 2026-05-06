package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildDelete;
import com.jftse.server.core.shared.packets.guild.SMSGGuildDelete;

@PacketId(CMSGGuildDelete.PACKET_ID)
public class GuildDeleteRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildDelete> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildDeleteRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildDelete packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            Guild guild = guildMember.getGuild();
            guildService.remove(guild.getId());
            connection.sendTCP(SMSGGuildDelete.builder().result((short) 0).build());
        }
    }
}
