package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildJoin;
import com.jftse.server.core.shared.packets.guild.SMSGGuildJoin;

import java.util.Date;

@PacketId(CMSGGuildJoin.PACKET_ID)
public class GuildJoinRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildJoin> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildJoinRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildJoin guildJoinRequestPacket) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getWaitingForApproval()) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -3).build());
            return;
        }

        if (guildMember != null) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -2).build());
            return;
        }

        Guild guild = guildService.findById((long) guildJoinRequestPacket.getGuildId());
        if (guild == null) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -1).build());
            return;
        }

        if (guild.getMemberList().stream().filter(x -> !x.getWaitingForApproval()).count() >= guild.getMaxMemberCount()) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -7).build());
            return;
        }

        if (activePlayer.getLevel() < guild.getLevelRestriction()) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -4).build());
            return;
        }

        boolean characterAllowed = false;
        for (byte type : guild.getAllowedCharacterType()) {
            characterAllowed = type == activePlayer.getPlayerType();
            if (characterAllowed) break;
        }

        if (!characterAllowed) {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) -5).build());
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
            connection.sendTCP(SMSGGuildJoin.builder().result((short) 1).build());
            connection.sendTCP(new S2CGuildDataAnswerPacket((short) 0, guild));
        } else {
            connection.sendTCP(SMSGGuildJoin.builder().result((short) 0).build());
        }
    }
}
