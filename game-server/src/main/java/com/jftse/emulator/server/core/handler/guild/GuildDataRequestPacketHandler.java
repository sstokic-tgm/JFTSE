package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildData;

@PacketId(CMSGGuildData.PACKET_ID)
public class GuildDataRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildData> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildDataRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildData packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
            return;
        }

        FTPlayer activePlayer = client.getPlayer();
        boolean waitingForApproval = guildMemberService.isWaitingForApproval(activePlayer.getId());
        Guild guild = guildService.findWithMembersByPlayerId(activePlayer.getId());

        if (guild == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (waitingForApproval)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guild));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guild));
    }
}
