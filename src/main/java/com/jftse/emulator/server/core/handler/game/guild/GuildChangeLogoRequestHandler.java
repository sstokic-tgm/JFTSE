package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChangeLogoRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildChangeLogoAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.core.service.PlayerPocketService;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildChangeLogoRequestHandler extends AbstractHandler {
    private C2SGuildChangeLogoRequestPacket c2SGuildChangeLogoRequestPacket;

    private final GuildService guildService;
    private final GuildMemberService guildMemberService;
    private final PlayerPocketService playerPocketService;

    public GuildChangeLogoRequestHandler() {
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        playerPocketService = ServiceManager.getInstance().getPlayerPocketService();
    }

    @Override
    public boolean process(Packet packet) {
        c2SGuildChangeLogoRequestPacket = new C2SGuildChangeLogoRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        Player player = connection.getClient().getPlayer();
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

            S2CGuildChangeLogoAnswerPacket answer = new S2CGuildChangeLogoAnswerPacket((short) 0);
            connection.sendTCP(answer);
        } else {
            connection.sendTCP(new S2CGuildChangeLogoAnswerPacket((short) -2));
        }
    }
}
