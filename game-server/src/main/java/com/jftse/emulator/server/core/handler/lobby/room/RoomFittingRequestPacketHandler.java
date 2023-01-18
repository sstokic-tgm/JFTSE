package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomFittingRequestPacket;
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

@PacketOperationIdentifier(PacketOperations.C2SRoomFittingReq)
public class RoomFittingRequestPacketHandler extends AbstractPacketHandler {
    private C2SRoomFittingRequestPacket roomFittingRequestPacket;

    @Override
    public boolean process(Packet packet) {
        roomFittingRequestPacket = new C2SRoomFittingRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null || client.getPlayer() == null)
            return;

        boolean fitting = roomFittingRequestPacket.isFitting();

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            roomPlayer.setFitting(fitting);

            short roomPlayerPosition = roomPlayer.getPosition();
            boolean shouldUpdateNonGM = roomPlayerPosition != MiscConstants.InvisibleGmSlot || !client.isGameMaster();

            Room room = client.getActiveRoom();
            if (room != null) {
                S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(room.getRoomPlayerList()));

                List<RoomPlayer> filteredRoomPlayerList = room.getRoomPlayerList().stream()
                        .filter(x -> x.getPosition() != MiscConstants.InvisibleGmSlot)
                        .collect(Collectors.toList());
                S2CRoomPlayerInformationPacket roomPlayerInformationPacketWithoutInvisibleGm =
                        new S2CRoomPlayerInformationPacket(new ArrayList<>(filteredRoomPlayerList));

                GameManager.getInstance().getClientsInRoom(room.getRoomId()).forEach(c -> {
                    RoomPlayer cRP = c.getRoomPlayer();
                    if (c.getConnection() != null && cRP != null && cRP.getPosition() == MiscConstants.InvisibleGmSlot) {
                        c.getConnection().sendTCP(roomPlayerInformationPacket);
                    } else {
                        if (shouldUpdateNonGM) {
                            c.getConnection().sendTCP(roomPlayerInformationPacketWithoutInvisibleGm);
                        }
                    }
                });
            }
        }
    }
}
