package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.GuildService;
import com.jftse.server.core.service.PlayerPocketService;
import com.jftse.server.core.shared.packets.guild.CMSGGuildChangeLogo;
import com.jftse.server.core.shared.packets.guild.SMSGGuildChangeLogo;

@PacketId(CMSGGuildChangeLogo.PACKET_ID)
public class GuildChangeLogoRequestHandler implements PacketHandler<FTConnection, CMSGGuildChangeLogo> {
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;
    private final PlayerPocketService playerPocketService;

    public GuildChangeLogoRequestHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public void handle(FTConnection connection, CMSGGuildChangeLogo c2SGuildChangeLogoRequestPacket) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        FTPlayer player = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(player.getId());

        if (guildMember != null && guildMember.getMemberRank() == 3) {
            Guild guild = guildMember.getGuild();
            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground() > 0) {
                PlayerPocket backgroundImagePocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground());
                guild.setLogoBackgroundId(backgroundImagePocket.getItemIndex());
                guild.setLogoBackgroundColor(c2SGuildChangeLogoRequestPacket.getLogoBackgroundColor());
            } else {
                guild.setLogoBackgroundId(-1);
                guild.setLogoBackgroundColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern() > 0) {
                PlayerPocket patternPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern());
                guild.setLogoPatternId(patternPocket.getItemIndex());
                guild.setLogoPatternColor(c2SGuildChangeLogoRequestPacket.getLogoPatternColor());
            } else {
                guild.setLogoPatternId(-1);
                guild.setLogoPatternColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark() > 0) {
                PlayerPocket markPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark());
                guild.setLogoMarkId(markPocket.getItemIndex());
                guild.setLogoMarkColor(c2SGuildChangeLogoRequestPacket.getLogoMarkColor());
            } else {
                guild.setLogoMarkId(-1);
                guild.setLogoMarkColor(-1);
            }

            guildService.save(guild);

            SMSGGuildChangeLogo answer = SMSGGuildChangeLogo.builder()
                    .result((short) 0)
                    .build();
            connection.sendTCP(answer);
        } else {
            connection.sendTCP(SMSGGuildChangeLogo.builder().result((short) -2).build());
        }
    }
}
