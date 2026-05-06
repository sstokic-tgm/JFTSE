package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserInfoClothAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerService;
import com.jftse.server.core.shared.packets.lobby.CMSGLobbyUserInfoCloth;

@PacketId(CMSGLobbyUserInfoCloth.PACKET_ID)
public class LobbyUserInfoClothReqPacketHandler implements PacketHandler<FTConnection, CMSGLobbyUserInfoCloth> {
    private final PlayerService playerService;

    public LobbyUserInfoClothReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public void handle(FTConnection connection, CMSGLobbyUserInfoCloth packet) {
        Player player = playerService.findByIdFetched((long) packet.getPlayerId());
        char result = (char) (player == null ? 1 : 0);

        S2CLobbyUserInfoClothAnswerPacket lobbyUserInfoClothAnswerPacket = new S2CLobbyUserInfoClothAnswerPacket(result, player);
        connection.sendTCP(lobbyUserInfoClothAnswerPacket);
    }
}
