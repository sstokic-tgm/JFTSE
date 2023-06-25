package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomReadyChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@PacketOperationIdentifier(PacketOperations.C2SRoomReadyChange)
public class RoomReadyChangeRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomReadyChangeRequestPacket roomReadyChangeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomReadyChangeRequestPacket = new C2SRoomReadyChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();

        if (!ftClient.getIsGoingReady().compareAndSet(false, true)) {
            return;
        }

        Room room = ftClient.getActiveRoom();
        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
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

        ftClient.getIsGoingReady().set(false);
    }
}
