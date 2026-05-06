package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildNameCheck;
import com.jftse.server.core.shared.packets.guild.SMSGGuildNameCheck;

@PacketId(CMSGGuildNameCheck.PACKET_ID)
public class GuildNameCheckRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildNameCheck> {
    private final GuildService guildService;

    public GuildNameCheckRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildNameCheck packet) {
        if (guildService.findByName(packet.getName()) != null)
            connection.sendTCP(SMSGGuildNameCheck.builder().result((short) -1).build());
        else
            connection.sendTCP(SMSGGuildNameCheck.builder().result((short) 0).build());
    }
}
