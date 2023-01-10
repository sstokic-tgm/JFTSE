package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildCreateRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildCreateAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.Date;
import java.util.List;

public class GuildCreateRequestPacketHandler extends AbstractHandler {
    private C2SGuildCreateRequestPacket guildCreateRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildCreateRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildCreateRequestPacket = new C2SGuildCreateRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        String guildName = guildCreateRequestPacket.getName();
        if (guildName.length() < 2 || guildName.length() > 12 || guildService.findByName(guildName) != null) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -1)); // This name cannot be used as a Club name.
            return;
        }

        Player activePlayer = connection.getClient().getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);
        if (guildMember != null) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -2)); // You already have a Club.
            return;
        } else if (activePlayer.getGold() < 5000) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -3)); // You do not have enough gold to create a new Club
            return;
        } else if (activePlayer.getLevel() < 10) {
            connection.sendTCP(new S2CGuildCreateAnswerPacket((char) -4)); // Your level is too low to create a new Club.
            return;
        }

        Guild guild = new Guild();
        guild.setName(guildName);
        guild.setIntroduction(guildCreateRequestPacket.getIntroduction());
        guild.setIsPublic(guildCreateRequestPacket.isPublic());
        guild.setLevelRestriction(guildCreateRequestPacket.getLevelRestriction());
        guild.setAllowedCharacterType(guildCreateRequestPacket.getAllowedCharacterType());
        guildService.save(guild);

        guildMember = new GuildMember();
        guildMember.setGuild(guild);
        guildMember.setPlayer(activePlayer);
        guildMember.setMemberRank((byte) 3); // ClubMaster
        guildMember.setRequestDate(new Date());
        guildMember.setWaitingForApproval(false);
        guildMemberService.save(guildMember);

        activePlayer.setGold(activePlayer.getGold() - 5000);
        connection.getClient().savePlayer(activePlayer);

        guild.setMemberList(List.of(guildMember));
        connection.sendTCP(new S2CGuildCreateAnswerPacket((char) 0));
    }
}
