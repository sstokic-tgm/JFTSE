package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.constants.RoomStatus;
import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.matchplay.GameSessionManager;
import com.jftse.emulator.server.core.matchplay.room.GameSession;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.S2CDisconnectAnswerPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomInformationPacket;
import com.jftse.emulator.server.core.packet.packets.lobby.room.S2CRoomPlayerInformationPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerInfoPlayStatsPacket;
import com.jftse.emulator.server.core.packet.packets.player.S2CPlayerStatusPointChangePacket;
import com.jftse.emulator.server.core.service.ClothEquipmentService;
import com.jftse.emulator.server.core.service.PlayerService;
import com.jftse.emulator.server.core.service.PlayerStatisticService;
import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.database.model.player.PlayerStatistic;
import com.jftse.emulator.server.database.model.player.StatusPointsAddedDto;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;

public class ClientBackInRoomPacketHandler extends AbstractHandler {
    private final PlayerService playerService;
    private final PlayerStatisticService playerStatisticService;
    private final ClothEquipmentService clothEquipmentService;

    public ClientBackInRoomPacketHandler() {
        playerService = ServiceManager.getInstance().getPlayerService();
        playerStatisticService = ServiceManager.getInstance().getPlayerStatisticService();
        clothEquipmentService = ServiceManager.getInstance().getClothEquipmentService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (connection.getClient() == null || connection.getClient().getActivePlayer() == null) {
            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
            connection.close();
            return;
        }

        Room currentClientRoom = connection.getClient().getActiveRoom();
        if (currentClientRoom == null) { // shouldn't happen
            S2CDisconnectAnswerPacket disconnectAnswerPacket = new S2CDisconnectAnswerPacket();
            connection.sendTCP(disconnectAnswerPacket);
            connection.close();
            return;
        }

        short position = currentClientRoom.getRoomPlayerList().stream()
                .filter(rp -> rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId()))
                .findAny()
                .get()
                .getPosition();

        Packet backInRoomAckPacket = new Packet(PacketOperations.S2CMatchplayClientBackInRoomAck.getValueAsChar());
        backInRoomAckPacket.write(position);
        connection.sendTCP(backInRoomAckPacket);

        Packet unsetHostPacket = new Packet(PacketOperations.S2CUnsetHost.getValueAsChar());
        unsetHostPacket.write((byte) 0);
        connection.sendTCP(unsetHostPacket);

        currentClientRoom.getRoomPlayerList().forEach(rp -> {
            if (rp.getPlayer().getId().equals(connection.getClient().getActivePlayer().getId())) {
                synchronized (rp) {
                    rp.setReady(false);
                }
            }
        });

        synchronized (currentClientRoom) {
            currentClientRoom.setStatus(RoomStatus.NotRunning);
        }

        Player player = playerService.findByIdFetched(connection.getClient().getActivePlayer().getId());
        PlayerStatistic playerStatistic = playerStatisticService.findPlayerStatisticById(player.getPlayerStatistic().getId());
        player.setPlayerStatistic(playerStatistic);
        player = playerService.save(player);
        connection.getClient().setActivePlayer(player);

        StatusPointsAddedDto statusPointsAddedDto = clothEquipmentService.getStatusPointsFromCloths(player);

        S2CPlayerStatusPointChangePacket playerStatusPointChangePacket = new S2CPlayerStatusPointChangePacket(player, statusPointsAddedDto);
        S2CPlayerInfoPlayStatsPacket playerInfoPlayStatsPacket = new S2CPlayerInfoPlayStatsPacket(playerStatistic);
        S2CRoomInformationPacket roomInformationPacket = new S2CRoomInformationPacket(currentClientRoom);
        S2CRoomPlayerInformationPacket roomPlayerInformationPacket = new S2CRoomPlayerInformationPacket(new ArrayList<>(currentClientRoom.getRoomPlayerList()));
        connection.sendTCP(playerStatusPointChangePacket, playerInfoPlayStatsPacket);
        connection.sendTCP(roomInformationPacket, roomPlayerInformationPacket);

        GameSession gameSession = connection.getClient().getActiveGameSession();
        if (gameSession != null) {
            connection.getClient().setActiveGameSession(null);
            if (gameSession.getClients().isEmpty()) {
                GameSessionManager.getInstance().removeGameSession(gameSession);
            }
        }
    }
}
