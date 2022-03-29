package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildJoinRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildJoinAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Date;

public class GuildJoinRequestPacketHandler extends AbstractHandler {
    private C2SGuildJoinRequestPacket guildJoinRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;
    private final PlayerService playerService;

    public GuildJoinRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        guildJoinRequestPacket = new C2SGuildJoinRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getWaitingForApproval()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -3));
            return;
        }

        if (guildMember != null) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -2));
            return;
        }

        Guild guild = guildService.findById((long) guildJoinRequestPacket.getGuildId());
        if (guild == null) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -1));
            return;
        }

        if (guild.getMemberList().stream().filter(x -> !x.getWaitingForApproval()).count() >= guild.getMaxMemberCount()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -7));
            return;
        }

        if (activePlayer.getLevel() < guild.getLevelRestriction()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -4));
            return;
        }

        boolean characterAllowed = false;
        for (byte type : guild.getAllowedCharacterType()) {
            characterAllowed = type == activePlayer.getPlayerType();
            if (characterAllowed) break;
        }

        if (!characterAllowed) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) -5));
            return;
        }

        guildMember = new GuildMember();
        guildMember.setGuild(guild);
        guildMember.setPlayer(activePlayer);
        guildMember.setMemberRank((byte) 1);
        guildMember.setRequestDate(new Date());
        guildMember.setWaitingForApproval(!guild.getIsPublic());
        guildMemberService.save(guildMember);

        if (guild.getIsPublic()) {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) 1));
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guild));
        } else {
            connection.sendTCP(new S2CGuildJoinAnswerPacket((short) 0));
        }
    }
}
