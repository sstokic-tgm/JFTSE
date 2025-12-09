package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserInfoAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;
import com.jftse.server.core.shared.packets.lobby.CMSGLobbyUserInfo;

@PacketId(CMSGLobbyUserInfo.PACKET_ID)
public class LobbyUserInfoReqPacketHandler implements PacketHandler<FTConnection, CMSGLobbyUserInfo> {
    private final PlayerService playerService;
    private final GuildMemberService guildMemberService;
    private final SocialService socialService;

    public LobbyUserInfoReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        guildMemberService = ServiceManager.getInstance().getGuildMemberService();
        socialService = ServiceManager.getInstance().getSocialService();
    }

    @Override
    public void handle(FTConnection connection, CMSGLobbyUserInfo packet) {
        Player player = playerService.findByIdFetched((long) packet.getPlayerId());
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
