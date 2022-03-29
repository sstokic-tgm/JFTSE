package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomReadyChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;

public class RoomReadyChangeRequestPacketHandler extends AbstractHandler {
    private C2SRoomReadyChangeRequestPacket roomReadyChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomReadyChangeRequestPacket = new C2SRoomReadyChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        Room room = connection.getClient().getActiveRoom();
        if (room != null) {
            room.getRoomPlayerList().stream()
                    .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                    .findAny()
                    .ifPresent(rp -> {
                        synchronized (rp) {
                            rp.setReady(roomReadyChangeRequestPacket.isReady());
                        }
                    });

            S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));
            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                if (c.getConnection() != null && c.getConnection().isConnected()) {
                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                }
            });
        }
    }
}
