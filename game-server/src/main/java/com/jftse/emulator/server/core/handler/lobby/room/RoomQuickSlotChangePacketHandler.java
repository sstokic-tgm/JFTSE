package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomQuickSlotChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomQuickSlotChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomQuickSlotChange)
public class RoomQuickSlotChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomQuickSlotChangeRequestPacket changeRoomQuickSlotRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomQuickSlotRequestPacket = new C2SRoomQuickSlotChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setQuickSlot(changeRoomQuickSlotRequestPacket.isQuickSlot());
            }

            S2CRoomQuickSlotChangeAnswerPacket roomQuickSlotChangeAnswerPacket = new S2CRoomQuickSlotChangeAnswerPacket(room.isQuickSlot());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomQuickSlotChangeAnswerPacket, client.getConnection());
        }
    }
}
