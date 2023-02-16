package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildListAnswerPacket;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class GuildListRequestPacketHandler extends AbstractHandler {
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
