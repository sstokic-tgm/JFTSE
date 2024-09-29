package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.C2SLobbyUserInfoRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserInfoAnswerPacket;
import com.jftse.entities.database.model.guild.Guild;
import com.jftse.entities.database.model.guild.GuildMember;
import com.jftse.entities.database.model.messenger.Friend;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.GuildMemberService;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.service.SocialService;

@PacketOperationIdentifier(PacketOperations.C2SLobbyUserInfoRequest)
public class LobbyUserInfoReqPacketHandler extends AbstractPacketHandler {
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
