package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.core.constants.GameMode;
import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class S2CGameDisplayPlayerStatsPacket extends Packet {
    public S2CGameDisplayPlayerStatsPacket(Room room) {
        super(PacketOperations.S2CGameDisplayPlayerStats.getValueAsChar());

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
