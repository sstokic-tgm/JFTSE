package com.jftse.emulator.server.core.handler.guild;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.player.Player;
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
        if (client == null ||client.getPlayer() == null)
            return;

        Player player = client.getPlayer();
        GuildMember guildMember = guildMemberService.getByPlayer(player);

        if (guildMember.getMemberRank() == 3) {
            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground() > 0) {
                PlayerPocket backgroundImagePocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoBackground());
                guildMember.getGuild().setLogoBackgroundId(backgroundImagePocket.getItemIndex());
                guildMember.getGuild().setLogoBackgroundColor(c2SGuildChangeLogoRequestPacket.getLogoBackgroundColor());
            } else {
                guildMember.getGuild().setLogoBackgroundId(-1);
                guildMember.getGuild().setLogoBackgroundColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern() > 0) {
                PlayerPocket patternPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoPattern());
                guildMember.getGuild().setLogoPatternId(patternPocket.getItemIndex());
                guildMember.getGuild().setLogoPatternColor(c2SGuildChangeLogoRequestPacket.getLogoPatternColor());
            } else {
                guildMember.getGuild().setLogoPatternId(-1);
                guildMember.getGuild().setLogoPatternColor(-1);
            }

            if (c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark() > 0) {
                PlayerPocket markPocket = playerPocketService.findById((long) c2SGuildChangeLogoRequestPacket.getPocketIdLogoMark());
                guildMember.getGuild().setLogoMarkId(markPocket.getItemIndex());
                guildMember.getGuild().setLogoMarkColor(c2SGuildChangeLogoRequestPacket.getLogoMarkColor());
            } else {
                guildMember.getGuild().setLogoMarkId(-1);
                guildMember.getGuild().setLogoMarkColor(-1);
            }

            guildService.save(guildMember.getGuild());

            SMSGGuildChangeLogo answer = SMSGGuildChangeLogo.builder()
                    .result((short) 0)
                    .build();
            connection.sendTCP(answer);
        } else {
            connection.sendTCP(SMSGGuildChangeLogo.builder().result((short) -2).build());
        }
    }
}
