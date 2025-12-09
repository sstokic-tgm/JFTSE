package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeMap;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeMap;

@PacketId(CMSGRoomChangeMap.PACKET_ID)
public class RoomMapChangeRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeMap> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeMap packet) {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setMap(packet.getMap());
            }

            SMSGRoomChangeMap answer = SMSGRoomChangeMap.builder().map(packet.getMap()).build();
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(answer);
                }
            });
        }
    }
}
