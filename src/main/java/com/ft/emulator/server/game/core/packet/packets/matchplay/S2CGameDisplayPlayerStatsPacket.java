package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.database.model.player.Player;
import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CGameDisplayPlayerStatsPacket extends Packet {
    public S2CGameDisplayPlayerStatsPacket(Room room) {
        super(PacketID.S2CGameDisplayPlayerStats);

        List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();

        this.write((char)roomPlayerList.size());

        for (RoomPlayer roomPlayer : roomPlayerList) {
            Player player = roomPlayer.getPlayer();
            this.write(roomPlayer.getPosition());
            this.write(player.getName());
            this.write(player.getLevel());
            this.write(player.getPlayerStatistic().getBasicRecordWin());
            this.write(player.getPlayerStatistic().getBasicRecordLoss());
            this.write(player.getPlayerStatistic().getConsecutiveWins());
        }
    }
}
