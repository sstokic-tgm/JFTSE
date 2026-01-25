package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.common.utilities.StreamUtils;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildSearchAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildSearch;

import java.util.ArrayList;
import java.util.List;

@PacketId(CMSGGuildSearch.PACKET_ID)
public class GuildSearchRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildSearch> {
    private final GuildService guildService;

    public GuildSearchRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildSearch packet) {
        byte searchType = packet.getSearchType();

        switch (searchType) {
            case 0 -> {
                Guild guild = guildService.findWithMembersById((long) packet.getNumber());
                if (guild != null)
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(List.of(guild)));
                else
                    connection.sendTCP(new S2CGuildSearchAnswerPacket(new ArrayList<>()));
            }
            case 1 -> {
                List<Guild> guildList = new ArrayList<>(guildService.findAllByNameContaining(packet.getName()));
                StreamUtils.batches(guildList, 10).forEach(guilds -> connection.sendTCP(new S2CGuildSearchAnswerPacket(guilds)));
            }
            default -> {
            }
        }
    }
}
