package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomListAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeGameMode;

@PacketId(CMSGRoomChangeGameMode.PACKET_ID)
public class GameModeChangePacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeGameMode> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeGameMode packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            return;
        }

        Room room = client.getActiveRoom();

        if (room != null) {
            synchronized (room) {
                room.setMode(packet.getMode());
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });

            FTPlayer player = client.getPlayer();
            GameManager.getInstance().getClientsInLobby().forEach(c -> {
                boolean isActivePlayer = c.hasPlayer() && c.getPlayer().getId() == player.getId();
                if (isActivePlayer)
                    return;

                if (c.getConnection() != null) {
                    S2CRoomListAnswerPacket roomListAnswerPacket = new S2CRoomListAnswerPacket(GameManager.getInstance().getFilteredRoomsForClient(c));
                    c.getConnection().sendTCP(roomListAnswerPacket);
                }
            });
        }
    }
}
