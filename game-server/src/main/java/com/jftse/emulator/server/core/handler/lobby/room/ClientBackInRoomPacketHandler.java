package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.core.constants.MiscConstants;
import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomPlayerListInformationPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerInfoPlayStatsPacket;
import com.jftse.emulator.server.core.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.player.PlayerStatistic;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.PlayerStatisticService;
import com.jftse.server.core.shared.packets.lobby.room.CMSGClientBackInRoom;
import com.jftse.server.core.shared.packets.lobby.room.SMSGClientBackInRoom;
import com.jftse.server.core.shared.packets.player.SMSGSetCouplePoints;

import java.util.List;

@PacketId(CMSGClientBackInRoom.PACKET_ID)
public class ClientBackInRoomPacketHandler implements PacketHandler<FTConnection, CMSGClientBackInRoom> {
    private final PlayerStatisticService playerStatisticService;

    public ClientBackInRoomPacketHandler() {
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
    }

    @Override
    public void handle(FTConnection connection, CMSGClientBackInRoom packet) {
        FTClient client = connection.getClient();
        if (!client.hasPlayer()) {
            connection.close(); // ??
            return;
        }

        FTPlayer player = client.getPlayer();

        Room currentClientRoom = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (currentClientRoom == null || roomPlayer == null) { // shouldn't happen
            connection.close();
            return;
        }


        short position = roomPlayer.getPosition();
        SMSGClientBackInRoom backInRoomPacket = SMSGClientBackInRoom.builder().position(position).build();
        connection.sendTCP(backInRoomPacket);

        Packet unsetHostPacket = new Packet(PacketOperations.S2CUnsetHost);
        unsetHostPacket.write((byte) 0);
        connection.sendTCP(unsetHostPacket);

        roomPlayer.setReady(false);
        roomPlayer.setGameAnimationSkipReady(false);
        roomPlayer.getConnectedToRelay().set(false);

        synchronized (currentClientRoom) {
            currentClientRoom.setStatus(RoomStatus.NotRunning);
        }

        PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatisticId());

        SMSGSetCouplePoints couplePointsPacket = SMSGSetCouplePoints.builder().amount(player.getCouplePoints()).build();
        S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player);
        S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(playerStatistic);
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(currentClientRoom);

        List<RoomPlayer> filteredRoomPlayerList = position == MiscConstants.InvisibleGmSlot
                ? currentClientRoom.getRoomPlayerList().stream().toList()
                : currentClientRoom.getRoomPlayerList().stream()
                        .filter(x -> x.getPosition() != MiscConstants.InvisibleGmSlot)
                        .toList();
        S2CRoomPlayerListInformationPacket roomPlayerListInformationPacket = new S2CRoomPlayerListInformationPacket(filteredRoomPlayerList);

        connection.sendTCP(couplePointsPacket);
        connection.sendTCP(playerStatusPointChangePacket, playerInfoPlayStatsPacket);
        connection.sendTCP(roomInformationPacket, roomPlayerListInformationPacket);
    }
}
