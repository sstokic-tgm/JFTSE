package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.packets.lobby.C2SLobbyUserInfoClothRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserInfoClothAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerService;

@PacketOperationIdentifier(PacketOperations.C2SLobbyUserInfoClothRequest)
public class LobbyUserInfoClothReqPacketHandler extends AbstractPacketHandler {
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
