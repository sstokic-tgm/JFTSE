package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeName;

@PacketId(CMSGRoomChangeName.PACKET_ID)
public class RoomNameChangePacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeName> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeName packet) {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setRoomName(packet.getRoomName());
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });
        }
    }
}
