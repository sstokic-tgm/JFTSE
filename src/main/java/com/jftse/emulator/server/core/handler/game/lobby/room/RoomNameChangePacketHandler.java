package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomNameChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomNameChangePacketHandler extends AbstractHandler {
    private C2SRoomNameChangeRequestPacket changeRoomNameRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomNameRequestPacket = new C2SRoomNameChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setRoomName(changeRoomNameRequestPacket.getRoomName());
            }

            S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(room);
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });
        }
    }
}
