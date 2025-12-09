package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomUserInfoAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomUserInfo;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketId(CMSGRoomUserInfo.PACKET_ID)
public class RoomUserInfoReqPacketHandler implements PacketHandler<FTConnection, CMSGRoomUserInfo> {
    @Override
    public void handle(FTConnection connection, CMSGRoomUserInfo packet) {
        FTClient ftClient = connection.getClient();
        if (ftClient == null || ftClient.getPlayer() == null)
            return;

        Room room = ftClient.getActiveRoom();
        if (room != null) {
            final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            Optional<RoomPlayer> optRoomPlayer = roomPlayerList.stream()
                    .filter(rp -> rp.getPosition() == packet.getPosition() && rp.getPlayer().getName().equals(packet.getNickname()))
                    .findFirst();

            char result = optRoomPlayer.isPresent() ? (char) 0 : (char) 1;

            S2CRoomUserInfoAnswerPacket roomUserInfoAnswerPacket = new S2CRoomUserInfoAnswerPacket(result, optRoomPlayer.orElse(null));
            connection.sendTCP(roomUserInfoAnswerPacket);
        }
    }
}
