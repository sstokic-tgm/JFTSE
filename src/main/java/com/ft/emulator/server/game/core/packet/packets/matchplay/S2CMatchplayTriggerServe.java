package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerServe extends Packet {
    public S2CMatchplayTriggerServe() {
        super(PacketID.S2CMatchplayStartServe);
        this.write((char) 1);
        this.write((char)0); // Player position (identic to position in room) this is just some kind of selector
        this.write(0); // Unk0
        this.write(0); // Position to side of playfield (0 red, 1 blue)
        this.write((byte)1); // Will serve ball (0 false, 1 true)
    }
}