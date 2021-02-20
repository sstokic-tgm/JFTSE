package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.ServeInfo;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySpawnBossBattle extends Packet {
    public S2CMatchplaySpawnBossBattle(byte bossIndex, byte leftMonsterIndex, byte rightMonsterIndex) {
        super(PacketID.S2CMatchplaySpawnBossBattle);

        this.write(bossIndex); // Boss index (BossGuardianInfo.set + 3)
        this.write(leftMonsterIndex); // Left Sideboss index (GuardianInfo.set)
        this.write(rightMonsterIndex); // Right Sideboss index (GuardianInfo.set)
    }
}