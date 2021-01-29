package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplaySetGameResultData extends Packet {
    public S2CMatchplaySetGameResultData(int[] playerPosSortedByPerformance) {
        super(PacketID.S2CMatchplaySetGameResultData);

        this.write((byte) playerPosSortedByPerformance.length);
        for (int i = 0; i < playerPosSortedByPerformance.length; i++) {
            int playerPos = playerPosSortedByPerformance[i];
            this.write(playerPos);
            this.write(150); // EXP
            this.write(200); // GOLD
            this.write(1); // Bonuses
        }
    }
}