package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.common.utilities.StringUtils;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomIsPrivateChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomIsPrivateChangePacketHandler extends AbstractHandler {
    private C2SRoomIsPrivateChangeRequestPacket changeRoomIsPrivateRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomIsPrivateRequestPacket = new C2SRoomIsPrivateChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        String password = changeRoomIsPrivateRequestPacket.getPassword();
        Room room = connection.getClient().getActiveRoom();
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
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomInformationPacket);
                }
            });
        }
    }
}
