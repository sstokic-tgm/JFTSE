package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(GuardianBase firstMonster, GuardianBase secondMonster, GuardianBase thirdMonster) {
        super(PacketOperations.S2CRoomSetGuardians);

        // (GuardianInfo.set)
        this.write(firstMonster == null ? (byte) 0 : firstMonster.getId().byteValue());
        this.write(secondMonster == null ? (byte) 0 : secondMonster.getId().byteValue());
        this.write(thirdMonster == null ? (byte) 0 : thirdMonster.getId().byteValue());
    }
}