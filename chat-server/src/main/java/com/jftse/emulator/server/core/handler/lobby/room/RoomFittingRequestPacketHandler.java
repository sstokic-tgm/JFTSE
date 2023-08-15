package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomFittingRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomFittingAnswerPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomFittingPlayerInfoPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

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
            final boolean oldFitting = roomPlayer.isFitting();
            roomPlayer.setFitting(fitting);

            S2CRoomFittingAnswerPacket roomFittingAnswerPacket = new S2CRoomFittingAnswerPacket(roomPlayer.getPosition(), roomPlayer.isFitting());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomFittingAnswerPacket, client.getConnection());

            if (oldFitting && !fitting) {
                S2CRoomFittingPlayerInfoPacket roomFittingPlayerInfoPacket = new S2CRoomFittingPlayerInfoPacket(roomPlayer.getPosition(), roomPlayer);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomFittingPlayerInfoPacket, client.getConnection());
            }
        }
    }
}
