package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeIsPrivate;

@PacketId(CMSGRoomChangeIsPrivate.PACKET_ID)
public class RoomIsPrivateChangePacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeIsPrivate> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeIsPrivate packet) {
        FTClient client = connection.getClient();

        String password = packet.getPassword();
        Room room = client.getActiveRoom();
        if (room != null) {
            if (StringUtils.isEmpty(password)) {
                synchronized (room) {
                    room.setPassword(null);
                    room.setPrivate(false);
                }
            } else {
                synchronized (room) {
                    room.setPassword(password);
                    room.setPrivate(true);
                }
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
