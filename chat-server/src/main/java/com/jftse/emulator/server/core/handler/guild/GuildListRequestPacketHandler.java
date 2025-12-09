package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildListAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildList;

import java.util.List;

@PacketId(CMSGGuildList.PACKET_ID)
public class GuildListRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildList> {
    private final GuildService guildService;

    public GuildListRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildList guildListRequestPacket) {
        if (guildListRequestPacket.getPage() == 0) {
            List<Guild> guildList = this.guildService.findAll();
            StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildListAnswerPacket(guilds)));
        }
    }
}
