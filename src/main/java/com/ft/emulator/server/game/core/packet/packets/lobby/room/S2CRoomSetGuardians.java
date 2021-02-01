package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(byte leftMonsterIndex, byte middleMonsterIndex, byte rightMonsterIndex) {
        super(PacketID.S2CRoomSetGuardians);

        // (GuardianInfo.set)
        this.write(leftMonsterIndex);
        this.write(middleMonsterIndex);
        this.write(rightMonsterIndex);
    }
}