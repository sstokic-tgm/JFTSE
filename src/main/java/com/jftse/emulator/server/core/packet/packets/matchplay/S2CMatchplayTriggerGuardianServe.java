package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerGuardianServe extends Packet {
    public S2CMatchplayTriggerGuardianServe(byte teamSide, byte xOffset, byte ballAngle) {
        super(PacketOperations.S2CMatchplayStartGuardianServe.getValueAsChar());

        this.write(teamSide);
        this.write(xOffset);
        this.write(ballAngle);
    }
}