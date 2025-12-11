package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeQuickSlot;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeQuickSlot;

@PacketId(CMSGRoomChangeQuickSlot.PACKET_ID)
public class RoomQuickSlotChangePacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeQuickSlot> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeQuickSlot packet) {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setQuickSlot(packet.getEnable());
            }

            SMSGRoomChangeQuickSlot answer = SMSGRoomChangeQuickSlot.builder().enable(room.isQuickSlot()).build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(answer, connection);
        }
    }
}
