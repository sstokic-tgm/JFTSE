package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplaySpawnBossBattle extends Packet {
    public S2CMatchplaySpawnBossBattle(GuardianBase boss, GuardianBase leftMonster, GuardianBase rightMonster) {
        super(PacketOperations.S2CMatchplaySpawnBossBattle);

        this.write(boss == null ? (byte) 0 : boss.getGuardIndex().byteValue()); // Boss index (BossGuardianInfo.set + 3)
        this.write(leftMonster == null ? (byte) 0 : leftMonster.getGuardIndex().byteValue()); // Left Sideboss index (GuardianInfo.set)
        this.write(rightMonster == null ? (byte) 0 : rightMonster.getGuardIndex().byteValue()); // Right Sideboss index (GuardianInfo.set)
    }
}
