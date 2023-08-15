package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CMatchplayDamageToPlayer extends Packet {
    public S2CMatchplayDamageToPlayer() {
        super(PacketOperations.S2CMatchplayDamageToPlayer);

        this.write((short) 0); //0 = Guard, 4 = Flame (only once)
        this.write((short) 0); //Damage
        this.write((short) 0); //Unk
        this.write((byte) 0); //Unk: > 0 = always die?
        this.write(0); //Unk
        this.write(0); //Unk
    }
}