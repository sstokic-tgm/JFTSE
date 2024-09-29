package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomListRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SRoomListReq)
public class RoomListRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomListRequestPacket roomListRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomListRequestPacket = new C2SRoomListRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null)
            return;

        FTClient client = (FTClient) connection.getClient();

        int roomType = roomListRequestPacket.getRoomTypeTab();
        int gameMode;
        switch (roomType) {
            case 256:
                gameMode = GameMode.GUARDIAN;
                break;
            case 192:
                gameMode = GameMode.BATTLE;
                break;
            case 48:
                gameMode = GameMode.BASIC;
                break;
            case 1536:
                gameMode = GameMode.BATTLEMON;
                break;
            default:
                gameMode = GameMode.ALL;
                break;
        }

        short direction = roomListRequestPacket.getDirection() == 0 ? (short) -1 : (short) 1;
        final short currentLobbyRoomListPage = (short) client.getLobbyCurrentRoomListPage();
        short newCurrentLobbyRoomListPage = currentLobbyRoomListPage;

        boolean wantsToGoBackOnNegativePage = direction == -1 && currentLobbyRoomListPage == 0;
        if (wantsToGoBackOnNegativePage) {
            direction = 0;
        }

        final int currentRoomType = client.getLobbyGameModeTabFilter();
        int availableRoomsCount = (int) GameManager.getInstance().getRooms().stream()
                .filter(x -> currentRoomType == GameMode.ALL || GameManager.getInstance().getRoomMode(x) == currentRoomType)
                .count();

        int possibleRoomsDisplayed = (currentLobbyRoomListPage + 1) * 5;
        if (direction == -1 || availableRoomsCount > possibleRoomsDisplayed) {
            newCurrentLobbyRoomListPage += direction;
        }

        if (currentRoomType != gameMode || currentLobbyRoomListPage < 0) {
            newCurrentLobbyRoomListPage = 0;
        }

        client.setLobbyCurrentRoomListPage(newCurrentLobbyRoomListPage);
        client.setLobbyGameModeTabFilter(gameMode);

        List<Room> roomList = GameManager.getInstance().getFilteredRoomsForClient((FTClient) connection.getClient());
        S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(roomList);
        connection.sendTCP(roomListAnswerPacket);
    }
}
