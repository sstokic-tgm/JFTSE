package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplaySpawnBossBattle extends Packet {
    public S2CMatchplaySpawnBossBattle(byte bossIndex, byte leftMonsterIndex, byte rightMonsterIndex) {
        super(PacketOperations.S2CMatchplaySpawnBossBattle.getValueAsChar());

        this.write(bossIndex); // Boss index (BossGuardianInfo.set + 3)
        this.write(leftMonsterIndex); // Left Sideboss index (GuardianInfo.set)
        this.write(rightMonsterIndex); // Right Sideboss index (GuardianInfo.set)
    }
}