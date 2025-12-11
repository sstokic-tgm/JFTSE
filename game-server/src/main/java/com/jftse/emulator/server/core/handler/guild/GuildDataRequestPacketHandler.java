package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildData;

@PacketId(CMSGGuildData.PACKET_ID)
public class GuildDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildData> {
    private final GuildMemberService guildMemberService;

    public GuildDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildData packet) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null) {
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
            return;
        }

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (guildMember.getWaitingForApproval())
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guildMember.getGuild()));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guildMember.getGuild()));
    }
}
