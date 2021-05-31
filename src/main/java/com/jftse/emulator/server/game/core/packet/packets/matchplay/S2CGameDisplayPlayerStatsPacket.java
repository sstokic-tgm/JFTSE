package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.database.model.player.Player;
import com.jftse.emulator.server.game.core.constants.GameMode;
import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CGameDisplayPlayerStatsPacket extends Packet {
    public S2CGameDisplayPlayerStatsPacket(Room room) {
        super(PacketID.S2CGameDisplayPlayerStats);

        List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
        short gameMode = room.getMode();

        this.write((char)roomPlayerList.size());

        for (RoomPlayer roomPlayer : roomPlayerList) {
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
