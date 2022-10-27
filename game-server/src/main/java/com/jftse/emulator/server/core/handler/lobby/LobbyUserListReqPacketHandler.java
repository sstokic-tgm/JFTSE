package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.packets.lobby.C2SLobbyUserListRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SLobbyUserListRequest)
public class LobbyUserListReqPacketHandler extends AbstractPacketHandler {
    private C2SLobbyUserListRequestPacket lobbyUserListRequestPacket;

    @Override
    public boolean process(Packet packet) {
        lobbyUserListRequestPacket = new C2SLobbyUserListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();

        byte page = lobbyUserListRequestPacket.getPage();
        final int clientLobbyCurrentPlayerListPage = client.getLobbyCurrentPlayerListPage();
        boolean shouldJustRefresh = lobbyUserListRequestPacket.getRefresh() == 0 & page == 1;
        boolean wantsToGoBackOnNegativePage = page == -1 && clientLobbyCurrentPlayerListPage == 1;
        if (wantsToGoBackOnNegativePage || shouldJustRefresh) {
            page = 0;
        }

        int newClientLobbyCurrentPlayerListPage = clientLobbyCurrentPlayerListPage + page;
        client.setLobbyCurrentPlayerListPage(newClientLobbyCurrentPlayerListPage);
        List<Player> lobbyPlayerList = GameManager.getInstance().getPlayersInLobby().stream()
                .skip(newClientLobbyCurrentPlayerListPage == 1 ? 0 : (newClientLobbyCurrentPlayerListPage * 10L) - 10)
                .limit(10)
                .collect(Collectors.toList());

        S2CLobbyUserListAnswerPacket lobbyUserListAnswerPacket = new S2CLobbyUserListAnswerPacket(lobbyPlayerList);
        connection.sendTCP(lobbyUserListAnswerPacket);
    }
}
