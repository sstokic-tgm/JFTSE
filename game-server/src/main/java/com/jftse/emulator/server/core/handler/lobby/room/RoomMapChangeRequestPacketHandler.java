package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomMapChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomMapChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomMapChange)
public class RoomMapChangeRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomMapChangeRequestPacket roomMapChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomMapChangeRequestPacket = new C2SRoomMapChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setMap(roomMapChangeRequestPacket.getMap());
            }

            S2CRoomMapChangeAnswerPacket roomMapChangeAnswerPacket = new S2CRoomMapChangeAnswerPacket(roomMapChangeRequestPacket.getMap());
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null) {
                    c.getConnection().sendTCP(roomMapChangeAnswerPacket);
                }
            });
        }
    }
}
