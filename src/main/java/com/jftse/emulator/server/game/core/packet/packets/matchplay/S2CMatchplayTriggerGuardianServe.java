package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerGuardianServe extends Packet {
    public S2CMatchplayTriggerGuardianServe() {
        super(PacketID.S2CMatchplayStartGuardianServe);

        this.write((byte) 0); //Teamside (0=Red, 1=Blue)
        this.write((byte) 0); //X-Offset
        this.write((byte) 0); //Ball angle(?)
    }
}