package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerGuardianServe extends Packet {
    public S2CMatchplayTriggerGuardianServe(byte teamSide, byte xOffset, byte ballAngle) {
        super(PacketID.S2CMatchplayStartGuardianServe);

        this.write(teamSide);
        this.write(xOffset);
        this.write(ballAngle);
    }
}