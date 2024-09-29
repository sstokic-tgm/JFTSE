package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomSlotCloseRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSlotCloseAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomSlotCloseReq)
public class RoomSlotCloseRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomSlotCloseRequestPacket roomSlotCloseRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomSlotCloseRequestPacket = new C2SRoomSlotCloseRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        if (!client.getIsClosingSlot().compareAndSet(false, true)) {
            return;
        }

        boolean deactivate = roomSlotCloseRequestPacket.isDeactivate();

        byte slot = roomSlotCloseRequestPacket.getSlot();
        Room room = client.getActiveRoom();
        if (room != null) {
            room.getPositions().set(slot, deactivate ? RoomPositionState.Locked : RoomPositionState.Free);

            S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket(slot, deactivate);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomSlotCloseAnswerPacket);
                }
            });
        }

        client.getIsClosingSlot().set(false);
    }
}
