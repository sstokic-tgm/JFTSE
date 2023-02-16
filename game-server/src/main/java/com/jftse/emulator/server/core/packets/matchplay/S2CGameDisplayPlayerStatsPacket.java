package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.player.Player;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class S2CGameDisplayPlayerStatsPacket extends Packet {
    public S2CGameDisplayPlayerStatsPacket(Room room) {
        super(PacketOperations.S2CGameDisplayPlayerStats);

        final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        short gameMode = room.getMode();
        List<RoomPlayer> activePlayers = roomPlayerList.stream()
                .filter(x -> x.getPosition() < 4)
                .collect(Collectors.toList());

        this.write((char)activePlayers.size());
        for (RoomPlayer roomPlayer : activePlayers) {
            Player player = roomPlayer.getPlayer();
            this.write(roomPlayer.getPosition());
            this.write(player.getName());
            this.write(player.getLevel());
            this.write(gameMode == GameMode.BASIC ? player.getPlayerStatistic().getBasicRecordWin() : player.getPlayerStatistic().getBattleRecordWin());
            this.write(gameMode == GameMode.BASIC ? player.getPlayerStatistic().getBasicRecordLoss() : player.getPlayerStatistic().getBattleRecordLoss());
            this.write(player.getPlayerStatistic().getConsecutiveWins());
        }
    }
}
