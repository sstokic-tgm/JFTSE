package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.packets.lobby.room.C2SRoomReadyChangeRequestPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        RoomPlayer roomPlayer = connection.getClient().getRoomPlayer();
        if (room != null && roomPlayer != null) {
            roomPlayer.setReady(roomReadyChangeRequestPacket.isReady());

            S2CRoomPlayerInformationPacket roomPlayerInformationPacket =
                    new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));

            List<RoomPlayer> filteredRoomPlayerList = room.getRoomPlayerList().stream()
                    .filter(x -> x.getPosition() != MiscConstants.InvisibleGmSlot)
                    .collect(Collectors.toList());
            S2CRoomPlayerInformationPacket roomPlayerInformationPacketWithoutInvisibleGm =
                    new S2CRoomPlayerInformationPacket(new ArrayList<>(filteredRoomPlayerList));

            GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                RoomPlayer cRP = c.getRoomPlayer();
                if (cRP != null && cRP.getPosition() == MiscConstants.InvisibleGmSlot) {
                    c.getConnection().sendTCP(roomPlayerInformationPacket);
                } else {
                    c.getConnection().sendTCP(roomPlayerInformationPacketWithoutInvisibleGm);
                }
            });
        }
    }
}
