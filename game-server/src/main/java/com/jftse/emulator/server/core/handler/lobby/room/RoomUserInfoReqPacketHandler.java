package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomUserInfoAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.PlayerStatisticService;
import com.jftse.server.core.shared.packets.lobby.room.CMSGRoomUserInfo;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedDeque;

@PacketId(CMSGRoomUserInfo.PACKET_ID)
public class RoomUserInfoReqPacketHandler implements PacketHandler<FTConnection, CMSGRoomUserInfo> {
    private final PlayerStatisticService playerStatisticService;

    public RoomUserInfoReqPacketHandler() {
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    @Override
    public void handle(FTConnection connection, CMSGRoomUserInfo packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer())
            return;

        Room room = ftClient.getActiveRoom();
        if (room != null) {
            final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            Optional<RoomPlayer> optRoomPlayer = roomPlayerList.stream()
                    .filter(rp -> rp.getPosition() == packet.getPosition() && rp.getName().equals(packet.getNickname()))
                    .findFirst();

            char result = optRoomPlayer.isPresent() ? (char) 0 : (char) 1;
            PlayerStatistic playerStatistic = null;
            if (optRoomPlayer.isPresent()) {
                playerStatistic = playerStatisticService.findPlayerStatisticById(optRoomPlayer.get().getPlayerStatisticId());
            }

            S2CRoomUserInfoAnswerPacket roomUserInfoAnswerPacket = new S2CRoomUserInfoAnswerPacket(result, optRoomPlayer.orElse(null), playerStatistic);
            connection.sendTCP(roomUserInfoAnswerPacket);
        }
    }
}
