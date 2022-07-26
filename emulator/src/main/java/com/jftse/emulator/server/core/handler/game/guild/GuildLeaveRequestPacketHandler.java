package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildLeaveAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildLeaveRequestPacketHandler extends AbstractHandler {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildLeaveRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
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
        if (guildMember.getMemberRank() == 3) {
            S2CGuildLeaveAnswerPacket guildLeaveAnswerPacket = new S2CGuildLeaveAnswerPacket((char) -2);
            connection.sendTCP(guildLeaveAnswerPacket);
        } else {
            Guild guild = guildMember.getGuild();
            guild.getMemberList().removeIf(x -> x.getId().equals(guildMember.getId()));
            guildService.save(guild);

            connection.sendTCP(new S2CGuildDataAnswerPacket((short) -2, guild));
        }
    }
}
