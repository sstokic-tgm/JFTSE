package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomFittingPlayerInfoPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomFitting;
import com.jftse.server.core.shared.packets.lobby.room.SMSGRoomFitting;

@PacketId(CMSGRoomFitting.PACKET_ID)
public class RoomFittingRequestPacketHandler implements PacketHandler<FTConnection, CMSGRoomFitting> {
    @Override
    public void handle(FTConnection connection, CMSGRoomFitting packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer())
            return;

        boolean fitting = packet.getFitting();

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer != null) {
            final boolean oldFitting = roomPlayer.isFitting();
            roomPlayer.setFitting(fitting);

            SMSGRoomFitting roomFittingAnswerPacket = SMSGRoomFitting.builder()
                    .position(roomPlayer.getPosition())
                    .ready(roomPlayer.isFitting())
                    .build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomFittingAnswerPacket, connection);

            if (oldFitting && !fitting) {
                S2CRoomFittingPlayerInfoPacket roomFittingPlayerInfoPacket = new S2CRoomFittingPlayerInfoPacket(roomPlayer.getPosition(), roomPlayer);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomFittingPlayerInfoPacket, connection);
            }
        }
    }
}
