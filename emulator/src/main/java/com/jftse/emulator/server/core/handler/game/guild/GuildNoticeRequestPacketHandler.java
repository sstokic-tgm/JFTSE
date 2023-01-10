package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildNoticeAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildNoticeRequestPacketHandler extends AbstractHandler {
    private final GuildMemberService guildMemberService;

    public GuildNoticeRequestPacketHandler() {
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null) {
            connection.sendTCP(new S2CGuildNoticeAnswerPacket(guildMember.getGuild().getNotice()));
        }
    }
}
