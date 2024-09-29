package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.C2SGuildNameCheckRequestPacket;
import com.jftse.emulator.server.core.packets.guild.S2CGuildNameCheckAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildService;

@PacketOperationIdentifier(PacketOperations.C2SGuildNameCheckRequest)
public class GuildNameCheckRequestPacketHandler extends AbstractPacketHandler {
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
