package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomUserInfoRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomUserInfoAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketOperationIdentifier(PacketOperations.C2SRoomUserInfoRequest)
public class RoomUserInfoReqPacketHandler extends AbstractPacketHandler {
    private C2SRoomUserInfoRequestPacket roomUserInfoRequestPacket;
    @Override
    public boolean process(Packet packet) {
        roomUserInfoRequestPacket = new C2SRoomUserInfoRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Room room = ftClient.getActiveRoom();
        if (room != null) {
            final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            Optional<RoomPlayer> optRoomPlayer = roomPlayerList.stream()
                    .filter(rp -> rp.getPosition() == roomUserInfoRequestPacket.getPosition() && rp.getPlayer().getName().equals(roomUserInfoRequestPacket.getNickname()))
                    .findFirst();

            char result = optRoomPlayer.isPresent() ? (char) 0 : (char) 1;

            S2CRoomUserInfoAnswerPacket roomUserInfoAnswerPacket = new S2CRoomUserInfoAnswerPacket(result, optRoomPlayer.orElse(null));
            connection.sendTCP(roomUserInfoAnswerPacket);
        }
    }
}
