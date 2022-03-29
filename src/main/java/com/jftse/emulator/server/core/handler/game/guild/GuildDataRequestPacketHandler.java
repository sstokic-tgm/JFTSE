package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildDataRequestPacketHandler extends AbstractHandler {
    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;

    public GuildDataRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null) {
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
            return;
        }

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (guildMember.getWaitingForApproval())
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guildMember.getGuild()));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guildMember.getGuild()));
    }
}
