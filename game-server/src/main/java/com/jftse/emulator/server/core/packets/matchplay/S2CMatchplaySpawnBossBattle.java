package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplaySpawnBossBattle extends Packet {
    public S2CMatchplaySpawnBossBattle(byte bossIndex, byte leftMonsterIndex, byte rightMonsterIndex) {
        super(PacketOperations.S2CMatchplaySpawnBossBattle);

        this.write(bossIndex); // Boss index (BossGuardianInfo.set + 3)
        this.write(leftMonsterIndex); // Left Sideboss index (GuardianInfo.set)
        this.write(rightMonsterIndex); // Right Sideboss index (GuardianInfo.set)
    }
}