package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomCloseSlot;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomCloseSlot;

@PacketId(CMSGRoomCloseSlot.PACKET_ID)
public class RoomSlotCloseRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomCloseSlot> {
    @Override
    public void handle(FTConnection connection, CMSGRoomCloseSlot packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        if (!client.getIsClosingSlot().compareAndSet(false, true)) {
            return;
        }

        boolean close = packet.getClose();

        byte slot = packet.getSlot();
        Room room = client.getActiveRoom();
        if (room != null) {
            room.getPositions().set(slot, close ? RoomPositionState.Locked : RoomPositionState.Free);

            SMSGRoomCloseSlot closeSlot = SMSGRoomCloseSlot.builder().slot(slot).close(close).build();
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(closeSlot);
                }
            });
        }

        client.getIsClosingSlot().set(false);
    }
}
