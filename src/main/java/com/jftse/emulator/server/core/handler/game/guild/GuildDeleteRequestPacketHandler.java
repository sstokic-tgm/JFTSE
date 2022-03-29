package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDeleteAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildDeleteRequestPacketHandler extends AbstractHandler {
    private final PlayerService playerService;
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildDeleteRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            Guild guild = guildMember.getGuild();
            guildService.remove(guild.getId());
            connection.sendTCP(new S2CGuildDeleteAnswerPacket((short) 0));
        }
    }
}
