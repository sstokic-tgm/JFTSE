package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(byte leftMonsterIndex, byte middleMonsterIndex, byte rightMonsterIndex) {
        super(PacketOperations.S2CRoomSetGuardians.getValueAsChar());

        // (GuardianInfo.set)
        this.write(leftMonsterIndex);
        this.write(middleMonsterIndex);
        this.write(rightMonsterIndex);
    }
}