package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomAllowBattlemonChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomAllowBattlemonChangePacketHandler extends AbstractHandler {
    private C2SRoomAllowBattlemonChangeRequestPacket changeRoomAllowBattlemonRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomAllowBattlemonRequestPacket = new C2SRoomAllowBattlemonChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            byte allowBattlemon = changeRoomAllowBattlemonRequestPacket.getAllowBattlemon() == 1 ? (byte) 2 : (byte) 0;
            // disable battlemon
            synchronized (room) {
                room.setAllowBattlemon((byte) 0);
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
