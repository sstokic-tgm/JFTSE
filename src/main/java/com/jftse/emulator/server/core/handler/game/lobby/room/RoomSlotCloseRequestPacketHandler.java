package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.RoomPositionState;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomSlotCloseRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomSlotCloseAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomSlotCloseRequestPacketHandler extends AbstractHandler {
    private C2SRoomSlotCloseRequestPacket roomSlotCloseRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomSlotCloseRequestPacket = new C2SRoomSlotCloseRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null)
            return;

        boolean deactivate = roomSlotCloseRequestPacket.isDeactivate();

        byte slot = roomSlotCloseRequestPacket.getSlot();
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            int positionsSize = room.getPositions().size();
            for (int i = 0; i < positionsSize; i++) {
                Short current = room.getPositions().poll();

                if (i == slot)
                    current = deactivate ? RoomPositionState.Locked : RoomPositionState.Free;

                room.getPositions().offer(current);
            }

            S2CRoomSlotCloseAnswerPacket roomSlotCloseAnswerPacket = new S2CRoomSlotCloseAnswerPacket(slot, deactivate);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomSlotCloseAnswerPacket);
                }
            });
        }
    }
}
