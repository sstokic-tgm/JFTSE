package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomIsPrivateChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomIsPrivateChange)
public class RoomIsPrivateChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomIsPrivateChangeRequestPacket changeRoomIsPrivateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomIsPrivateRequestPacket = new C2SRoomIsPrivateChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();

        String password = changeRoomIsPrivateRequestPacket.getPassword();
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
