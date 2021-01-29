package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySetGameResultData extends Packet {
    public S2CMatchplaySetGameResultData(List<Integer> playerPosSortedByPerformance) {
        super(PacketID.S2CMatchplaySetGameResultData);

        this.write((byte) playerPosSortedByPerformance.size());
        for (int i = 0; i < playerPosSortedByPerformance.size(); i++) {
            int playerPos = playerPosSortedByPerformance.get(i);
            this.write(playerPos);
            this.write(150); // EXP
            this.write(200); // GOLD
            this.write(1); // Bonuses
        }
    }
}