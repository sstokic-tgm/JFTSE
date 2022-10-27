package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomNameChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomNameChange)
public class RoomNameChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomNameChangeRequestPacket changeRoomNameRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomNameRequestPacket = new C2SRoomNameChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setRoomName(changeRoomNameRequestPacket.getRoomName());
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
