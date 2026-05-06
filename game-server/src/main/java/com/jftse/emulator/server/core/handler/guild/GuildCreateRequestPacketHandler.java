package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildCreate;
import com.jftse.server.core.shared.packets.guild.SMSGGuildCreate;

import java.util.Date;
import java.util.Set;

@PacketId(CMSGGuildCreate.PACKET_ID)
public class GuildCreateRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildCreate> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;
    private final PlayerService playerService;

    public GuildCreateRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildCreate guildCreateRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        String guildName = guildCreateRequestPacket.getName();
        if (guildName.length() < 2 || guildName.length() > 12 || guildService.findByName(guildName) != null) {
            connection.sendTCP(SMSGGuildCreate.builder().result((char) -1).build()); // This name cannot be used as a Club name.
            return;
        }

        FTPlayer activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer.getId());
        if (guildMember != null) {
            connection.sendTCP(SMSGGuildCreate.builder().result((char) -2).build()); // You already have a Club.
            return;
        } else if (activePlayer.getGold() < 5000) {
            connection.sendTCP(SMSGGuildCreate.builder().result((char) -3).build()); // You do not have enough gold to create a new Club
            return;
        } else if (activePlayer.getLevel() < 10) {
            connection.sendTCP(SMSGGuildCreate.builder().result((char) -4).build());  // Your level is too low to create a new Club.
            return;
        }

        Guild guild = new Guild();
        guild.setName(guildName);
        guild.setIntroduction(guildCreateRequestPacket.getIntroduction());
        guild.setIsPublic(guildCreateRequestPacket.getIsPublic());
        guild.setLevelRestriction(guildCreateRequestPacket.getLevelRestriction());
        guild.setAllowedCharacterType(guildCreateRequestPacket.getAllowedCharacterType().toArray(new Byte[0]));
        guildService.save(guild);

        guildMember = new GuildMember();
        guildMember.setGuild(guild);
        guildMember.setPlayer(activePlayer.getPlayerRef());
        guildMember.setMemberRank((byte) 3); // ClubMaster
        guildMember.setRequestDate(new Date());
        guildMember.setWaitingForApproval(false);
        guildMemberService.save(guildMember);

        int newGold = activePlayer.getGold() - 5000;
        activePlayer.syncGold(newGold);
        playerService.setMoney(activePlayer.getPlayer(),  newGold);

        guild.setMemberList(Set.of(guildMember));
        connection.sendTCP(SMSGGuildCreate.builder().result((char) 0).build());
    }
}
