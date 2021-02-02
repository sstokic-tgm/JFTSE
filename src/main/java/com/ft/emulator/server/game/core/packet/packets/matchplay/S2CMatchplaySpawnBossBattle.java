package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.ServeInfo;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CMatchplaySpawnBossBattle extends Packet {
    public S2CMatchplaySpawnBossBattle(List<ServeInfo> serveInfo) {
        super(PacketID.S2CMatchplaySpawnBossBattle);

        this.write((byte)0); // Boss index (BossGuardianInfo.set + 3)
        this.write((byte)0); // Left Sideboss index (GuardianInfo.set)
        this.write((byte)0); // Right Sideboss index (GuardianInfo.set)
    }
}