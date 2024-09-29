package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildListRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildListAnswerPacket;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SGuildListRequest)
public class GuildListRequestPacketHandler extends AbstractPacketHandler {
    private C2SGuildListRequestPacket guildListRequestPacket;

    private final GuildService guildService;

    public GuildListRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public boolean process(Packet packet) {
        guildListRequestPacket = new C2SGuildListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (guildListRequestPacket.getPage() == 0) {
            List<Guild> guildList = this.guildService.findAll();
            StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildListAnswerPacket(guilds)));
        }
    }
}
