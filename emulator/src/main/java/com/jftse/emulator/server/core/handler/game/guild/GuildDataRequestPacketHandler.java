package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildDataRequestPacketHandler extends AbstractHandler {
    private final GuildMemberService guildMemberService;

    public GuildDataRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null) {
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
            return;
        }

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember == null)
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, null));
        else if (guildMember.getWaitingForApproval())
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -1, guildMember.getGuild()));
        else
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guildMember.getGuild()));
    }
}
