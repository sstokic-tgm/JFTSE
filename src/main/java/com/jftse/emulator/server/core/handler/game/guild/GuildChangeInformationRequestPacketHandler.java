package com.jftse.emulator.server.core.handler.game.guild;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.guild.C2SGuildChangeInformationRequestPacket;
import com.jftse.emulator.server.core.packet.packets.guild.S2CGuildDataAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.GuildService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class GuildChangeInformationRequestPacketHandler extends AbstractHandler {
    private C2SGuildChangeInformationRequestPacket guildChangeInformationRequestPacket;

    private final PlayerService playerService;
    private final GuildService guildService;
    private final GuildMemberService guildMemberService;

    public GuildChangeInformationRequestPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildService = ServiceManager.getInstance().getGuildService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
    }

    @Override
    public boolean process(Packet packet) {
        guildChangeInformationRequestPacket = new C2SGuildChangeInformationRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null)
            return;

        Player activePlayer = playerService.findById(connection.getClient().getActivePlayer().getId());
        GuildMember guildMember = guildMemberService.getByPlayer(activePlayer);

        if (guildMember != null && guildMember.getMemberRank() > 1) {
            Guild guild = guildMember.getGuild();

            guild.setIntroduction(guildChangeInformationRequestPacket.getIntroduction());
            guild.setLevelRestriction(guildChangeInformationRequestPacket.getMinLevel());
            guild.setIsPublic(guildChangeInformationRequestPacket.isPublic());
            guild.setAllowedCharacterType(guildChangeInformationRequestPacket.getAllowedCharacterType());
            guildService.save(guild);

            connection.sendTCP(new S2CGuildDataAnswerPacket((byte) 0, guild));
        }
    }
}
