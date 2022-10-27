package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(byte leftMonsterIndex, byte middleMonsterIndex, byte rightMonsterIndex) {
        super(PacketOperations.S2CRoomSetGuardians.getValue());

        // (GuardianInfo.set)
        this.write(leftMonsterIndex);
        this.write(middleMonsterIndex);
        this.write(rightMonsterIndex);
    }
}