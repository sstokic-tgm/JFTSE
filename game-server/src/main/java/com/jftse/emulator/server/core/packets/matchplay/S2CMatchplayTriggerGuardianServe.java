package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CMatchplayTriggerGuardianServe extends Packet {
    public S2CMatchplayTriggerGuardianServe(byte teamSide, byte xOffset, byte ballAngle) {
        super(PacketOperations.S2CMatchplayStartGuardianServe);

        this.write(teamSide);
        this.write(xOffset);
        this.write(ballAngle);
    }
}