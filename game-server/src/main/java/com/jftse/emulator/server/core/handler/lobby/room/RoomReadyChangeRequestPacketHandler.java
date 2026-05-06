package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomChangeReady;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomChangeReady;

@PacketId(CMSGRoomChangeReady.PACKET_ID)
public class RoomReadyChangeRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomChangeReady> {
    @Override
    public void handle(FTConnection connection, CMSGRoomChangeReady packet) {
        FTClient ftClient = connection.getClient();

        if (!ftClient.getIsGoingReady().compareAndSet(false, true)) {
            return;
        }

        RoomPlayer roomPlayer = ftClient.getRoomPlayer();
        if (roomPlayer != null) {
            roomPlayer.setReady(packet.getReady());

            SMSGRoomChangeReady answer = SMSGRoomChangeReady.builder().position(roomPlayer.getPosition()).ready(roomPlayer.isReady()).build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(answer, connection);
        }

        ftClient.getIsGoingReady().set(false);
    }
}
