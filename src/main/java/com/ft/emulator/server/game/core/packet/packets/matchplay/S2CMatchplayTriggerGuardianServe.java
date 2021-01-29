package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerGuardianServer extends Packet {
    public S2CMatchplayTriggerGuardianServer() {
        super(PacketID.S2CMatchplayStartGuardianServe);

        this.write((byte) 0); //Teamside (0=Red, 1=Blue)
        this.write((byte) 0); //X-Offset
        this.write((byte) 0); //Unk
    }
}