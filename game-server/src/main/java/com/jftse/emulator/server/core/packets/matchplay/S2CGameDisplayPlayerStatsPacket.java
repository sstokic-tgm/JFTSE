package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.constants.GameMode;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CGameDisplayPlayerStatsPacket extends Packet {
    public S2CGameDisplayPlayerStatsPacket(List<RoomPlayer> roomPlayers, short gameMode) {
        super(PacketOperations.S2CGameDisplayPlayerStats);

        List<RoomPlayer> activePlayers = roomPlayers.stream()
                .filter(x -> x.getPosition() < 4)
                .toList();

        this.write((char)activePlayers.size());
        for (RoomPlayer roomPlayer : activePlayers) {
            this.write(roomPlayer.getPosition());
            this.write(roomPlayer.getName());
            this.write((byte) roomPlayer.getLevel());
            this.write(gameMode == GameMode.BASIC ? roomPlayer.getPlayerStatistic().basicRecordWin() :  roomPlayer.getPlayerStatistic().battleRecordWin());
            this.write(gameMode == GameMode.BASIC ? roomPlayer.getPlayerStatistic().basicRecordLoss() : roomPlayer.getPlayerStatistic().battleRecordLoss());
            this.write(roomPlayer.getPlayerStatistic().consecutiveWins());
        }
    }
}
