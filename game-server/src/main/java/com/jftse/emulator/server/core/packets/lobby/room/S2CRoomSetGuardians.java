package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.entities.database.model.battle.GuardianBase;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSetGuardians extends Packet {
    public S2CRoomSetGuardians(GuardianBase firstMonster, GuardianBase secondMonster, GuardianBase thirdMonster) {
        super(PacketOperations.S2CRoomSetGuardians);

        // (GuardianInfo.set)
        this.write(firstMonster.getId().byteValue());
        this.write(secondMonster.getId().byteValue());
        this.write(thirdMonster.getId().byteValue());
    }
}