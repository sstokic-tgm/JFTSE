package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomMapChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomMapChangeRequestPacketHandler extends AbstractHandler {
    private C2SRoomMapChangeRequestPacket roomMapChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomMapChangeRequestPacket = new C2SRoomMapChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setMap(roomMapChangeRequestPacket.getMap());
            }

            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(roomMapChangeRequestPacket.getMap());
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomMapChangeAnswerPacket);
                }
            });
        }
    }
}
