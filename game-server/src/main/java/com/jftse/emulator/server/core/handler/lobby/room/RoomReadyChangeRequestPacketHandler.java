package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomReadyChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomReadyChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

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

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer != null) {
            roomPlayer.setReady(roomReadyChangeRequestPacket.isReady());

            S2CRoomReadyChangeAnswerPacket roomReadyChangeAnswerPacket = new S2CRoomReadyChangeAnswerPacket(roomPlayer.getPosition(), roomPlayer.isReady());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomReadyChangeAnswerPacket, ftClient.getConnection());
        }

        ftClient.getIsGoingReady().set(false);
    }
}
