package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CMatchplayDamageToPlayer extends Packet {
    public S2CMatchplayDamageToPlayer() {
        super(PacketID.S2CMatchplayDamageToPlayer);

        this.write((short) 0); //0 = Guard, 4 = Flame (only once)
        this.write((short) 0); //Damage
        this.write((short) 0); //Unk
        this.write((byte) 0); //Unk: > 0 = always die?
        this.write(0); //Unk
        this.write(0); //Unk
    }
}