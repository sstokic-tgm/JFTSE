package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildNameCheckRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildNameCheckAnswerPacket;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildNameCheckRequestPacketHandler extends AbstractHandler {
    private C2SGuildNameCheckRequestPacket guildNameCheckRequestPacket;

    private final GuildService guildService;

    public GuildNameCheckRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public boolean process(Packet packet) {
        guildNameCheckRequestPacket = new C2SGuildNameCheckRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (guildService.findByName(guildNameCheckRequestPacket.getName()) != null)
            connection.sendTCP(new S2CGuildNameCheckAnswerPacket((short) -1));
        else
            connection.sendTCP(new S2CGuildNameCheckAnswerPacket((short) 0));
    }
}
