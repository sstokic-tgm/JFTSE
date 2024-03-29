package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomFittingRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.entities.database.model.player.Player;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;

public class RoomFittingRequestPacketHandler extends AbstractHandler {
    private C2SRoomFittingRequestPacket roomFittingRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomFittingRequestPacket = new C2SRoomFittingRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getPlayer() == null)
            return;

        boolean fitting = roomFittingRequestPacket.isFitting();

        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        if (roomPlayer != null) {
            roomPlayer.setFitting(fitting);

            Room room = connection.getClient().getActiveRoom();
            if (room != null) {
                S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));
                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                    if (c.getConnection() != null && c.getConnection().isConnected()) {
                        c.getConnection().sendTCP(roomPlayerInformationPacket);
                    }
                });
            }
        }
    }
}
