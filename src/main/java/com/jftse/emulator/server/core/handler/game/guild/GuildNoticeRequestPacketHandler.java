package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildNoticeAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildNoticeRequestPacketHandler extends AbstractHandler {
    private final GuildMemberService guildMemberService;
    private final PlayerService playerService;

    public GuildNoticeRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerService = ServiceManager.getInstance().getPlayerService();
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
        if (guildMember != null) {
            connection.sendTCP(new S2CGuildNoticeAnswerPacket(guildMember.getGuild().getNotice()));
        }
    }
}
