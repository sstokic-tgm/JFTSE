package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomListRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;
import java.util.stream.Collectors;

public class RoomListRequestPacketHandler extends AbstractHandler {
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
        final short currentLobbyRoomListPage = (short) connection.getClient().getLobbyCurrentRoomListPage();
        short newCurrentLobbyRoomListPage = currentLobbyRoomListPage;

        boolean wantsToGoBackOnNegativePage = direction == -1 && currentLobbyRoomListPage == 0;
        if (wantsToGoBackOnNegativePage) {
            direction = 0;
        }

        final int currentRoomType = connection.getClient().getLobbyGameModeTabFilter();
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

        connection.getClient().setLobbyCurrentRoomListPage(newCurrentLobbyRoomListPage);
        connection.getClient().setLobbyGameModeTabFilter(gameMode);

        List<Room> roomList = GameManager.getInstance().getFilteredRoomsForClient(connection.getClient());
        S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(roomList);
        connection.sendTCP(roomListAnswerPacket);
    }
}
