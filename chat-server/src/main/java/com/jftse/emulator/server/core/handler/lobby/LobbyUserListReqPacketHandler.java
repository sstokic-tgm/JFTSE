package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyUserListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.Player;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.CMSGLobbyUserList;

import java.util.List;
import java.util.stream.Collectors;

@PacketId(CMSGLobbyUserList.PACKET_ID)
public class LobbyUserListReqPacketHandler implements PacketHandler<FTConnection, CMSGLobbyUserList> {
    @Override
    public void handle(FTConnection connection, CMSGLobbyUserList packet) {
        FTClient client = connection.getClient();

        byte page = packet.getPage();
        final int clientLobbyCurrentPlayerListPage = client.getLobbyCurrentPlayerListPage();
        boolean shouldJustRefresh = packet.getRefresh() == 0 & page == 1;
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
