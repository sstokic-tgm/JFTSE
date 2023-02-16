package com.jftse.emulator.server.core.handler.game.lobby;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.lobby.C2SLobbyUserInfoClothRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.S2CLobbyUserInfoClothAnswerPacket;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

public class LobbyUserInfoClothReqPacketHandler extends AbstractHandler {
    private C2SLobbyUserInfoClothRequestPacket lobbyUserInfoClothRequestPacket;

    private final PlayerService playerService;

    public LobbyUserInfoClothReqPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
    }

    @Override
    public boolean process(Packet packet) {
        lobbyUserInfoClothRequestPacket = new C2SLobbyUserInfoClothRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Player player = playerService.findByIdFetched((long) lobbyUserInfoClothRequestPacket.getPlayerId());
        char result = (char) (player == null ? 1 : 0);

        S2CLobbyUserInfoClothAnswerPacket lobbyUserInfoClothAnswerPacket = new S2CLobbyUserInfoClothAnswerPacket(result, player);
        connection.sendTCP(lobbyUserInfoClothAnswerPacket);
    }
}
