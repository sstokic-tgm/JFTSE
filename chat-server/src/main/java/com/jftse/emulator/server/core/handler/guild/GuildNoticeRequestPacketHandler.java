package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildNotice;
import com.jftse.server.core.shared.packets.guild.SMSGGuildNotice;

@PacketId(CMSGGuildNotice.PACKET_ID)
public class GuildNoticeRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildNotice> {
    private final GuildMemberService guildMemberService;

    public GuildNoticeRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildNotice packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null) {
            connection.sendTCP(SMSGGuildNotice.builder().notice(guildMember.getGuild().getNotice()).build());
        }
    }
}
