package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(byte firstMonsterIndex, byte secondMonsterIndex, byte thirdMonsterIndex) {
        super(PacketOperations.S2CRoomSetGuardians);

        // (GuardianInfo.set)
        this.write(firstMonsterIndex);
        this.write(secondMonsterIndex);
        this.write(thirdMonsterIndex);
    }
}