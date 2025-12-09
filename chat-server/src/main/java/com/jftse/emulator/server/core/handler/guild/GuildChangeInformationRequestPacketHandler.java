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
import com.jftse.server.core.shared.packets.guild.CMSGGuildChangeInformation;

@PacketId(CMSGGuildChangeInformation.PACKET_ID)
public class GuildChangeInformationRequestPacketHandler implements PacketHandler<FTConnection, CMSGGuildChangeInformation> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildChangeInformationRequestPacketHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChangeInformation guildChangeInformationRequestPacket) {
        FTClient client = connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        Player activePlayer = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildMember.getGuild();

            guild.setIntroduction(guildChangeInformationRequestPacket.getIntroduction());
            guild.setLevelRestriction(guildChangeInformationRequestPacket.getMinLevel());
            guild.setIsPublic(guildChangeInformationRequestPacket.getIsPublic());
            guild.setAllowedCharacterType(guildChangeInformationRequestPacket.getAllowedCharacterTypes().toArray(new Byte[0]));
            guildService.save(guild);

            connection.sendTCP(new S2CGuildDataAnswerPacket((byte) 0, guild));
        }
    }
}
