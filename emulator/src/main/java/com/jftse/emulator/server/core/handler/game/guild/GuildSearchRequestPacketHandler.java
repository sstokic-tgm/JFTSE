package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildSearchRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildSearchAnswerPacket;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;

public class GuildSearchRequestPacketHandler extends AbstractHandler {
    private C2SGuildSearchRequestPacket guildSearchRequestPacket;

    private final GuildService guildService;

    public GuildSearchRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public boolean process(Packet packet) {
        guildSearchRequestPacket = new C2SGuildSearchRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        byte searchType = guildSearchRequestPacket.getSearchType();

        switch (searchType) {
            case 0 -> {
                Guild guild = guildService.findById((long) guildSearchRequestPacket.getNumber());
                if (guild != null)
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(List.of(guild)));
                else
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(new ArrayList<>()));
            }
            case 1 -> {
                List<Guild> guildList = new ArrayList<>(guildService.findAllByNameContaining(guildSearchRequestPacket.getName()));
                StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildSearchAnswerPacket(guilds)));
            }
            default -> {
            }
        }
    }
}
