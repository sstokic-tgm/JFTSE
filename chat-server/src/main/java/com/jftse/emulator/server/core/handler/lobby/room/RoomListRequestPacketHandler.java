package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.ChatMode;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomList;

import java.util.List;

@PacketId(CMSGRoomList.PACKET_ID)
public class RoomListRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomList> {
    @Override
    public void handle(FTConnection connection, CMSGRoomList packet) {
        FTClient client = connection.getClient();

        int roomType = packet.getRoomTypeTab();
        int gameMode = switch (roomType) {
            case 1 -> ChatMode.CHAT;
            case 2 -> ChatMode.MY_HOME;
            default -> ChatMode.ALL;
        };

        short direction = packet.getDirection() == 0 ? (short) -1 : (short) 1;
        final short currentLobbyRoomListPage = (short) client.getLobbyCurrentRoomListPage();
        short newCurrentLobbyRoomListPage = currentLobbyRoomListPage;

        boolean wantsToGoBackOnNegativePage = direction == -1 && currentLobbyRoomListPage == 0;
        if (wantsToGoBackOnNegativePage) {
            direction = 0;
        }

        final int currentRoomType = client.getLobbyGameModeTabFilter();
        int availableRoomsCount = (int) GameManager.getInstance().getRooms().stream()
                .filter(x -> currentRoomType == ChatMode.ALL || GameManager.getInstance().getChatMode(x) == currentRoomType)
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

        List<Room> roomList = GameManager.getInstance().getFilteredRoomsForClient(connection.getClient());
        S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(roomList);
        connection.sendTCP(roomListAnswerPacket);
    }
}
