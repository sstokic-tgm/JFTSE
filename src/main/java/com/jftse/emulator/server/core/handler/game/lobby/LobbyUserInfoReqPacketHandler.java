package com.jftse.emulator.server.core.handler.game.lobby;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.lobby.C2SLobbyUserInfoRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.S2CLobbyUserInfoAnswerPacket;
import com.jftse.emulator.server.core.service.GuildMemberService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.SocialService;
import com.jftse.emulator.server.database.model.guild.Guild;
import com.jftse.emulator.server.database.model.guild.GuildMember;
import com.jftse.emulator.server.database.model.messenger.Friend;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class LobbyUserInfoReqPacketHandler extends AbstractHandler {
    private C2SLobbyUserInfoRequestPacket lobbyUserInfoRequestPacket;

    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;
    private final SocialService socialService;

    public LobbyUserInfoReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public boolean process(Packet packet) {
        lobbyUserInfoRequestPacket = new C2SLobbyUserInfoRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findByIdFetched((long) lobbyUserInfoRequestPacket.getPlayerId());
        char result = (char) (player == null ? 1 : 0);

        GuildMember guildMember = guildMemberService.getByPlayer(player);
        Guild guild = null;
        if (guildMember != null && !guildMember.getWaitingForApproval() && guildMember.getGuild() != null)
            guild = guildMember.getGuild();

        Friend couple = socialService.getRelationship(player);
        S2CLobbyUserInfoAnswerPacket lobbyUserInfoAnswerPacket = new S2CLobbyUserInfoAnswerPacket(result, player, guild, couple);
        connection.sendTCP(lobbyUserInfoAnswerPacket);
    }
}
