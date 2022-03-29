package com.jftse.emulator.server.core.handler.game.lobby;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packet.packets.lobby.C2SLobbyUserListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class LobbyUserListReqPacketHandler extends AbstractHandler {
    private C2SLobbyUserListRequestPacket lobbyUserListRequestPacket;

    @Override
    public boolean process(Packet packet) {
        lobbyUserListRequestPacket = new C2SLobbyUserListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        byte page = lobbyUserListRequestPacket.getPage();
        int clientLobbyCurrentPlayerListPage = connection.getClient().getLobbyCurrentPlayerListPage();
        boolean shouldJustRefresh = lobbyUserListRequestPacket.getRefresh() == 0 & page == 1;
        boolean wantsToGoBackOnNegativePage = page == -1 && clientLobbyCurrentPlayerListPage == 1;
        if (wantsToGoBackOnNegativePage || shouldJustRefresh) {
            page = 0;
        }

        clientLobbyCurrentPlayerListPage += page;
        connection.getClient().setLobbyCurrentPlayerListPage(clientLobbyCurrentPlayerListPage);
        List<Player> lobbyPlayerList = GameManager.getInstance().getPlayersInLobby().stream()
                .skip(clientLobbyCurrentPlayerListPage == 1 ? 0 : (clientLobbyCurrentPlayerListPage * 10L) - 10)
                .limit(10)
                .collect(Collectors.toList());

        S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyPlayerList);
        connection.sendTCP(lobbyUserListAnswerPacket);
    }
}
