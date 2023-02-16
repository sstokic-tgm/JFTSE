package com.jftse.emulator.server.core.packets.challenge;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengeDamagePacket extends Packet {
    private byte player;
    private int dmg;

    public C2SChallengeDamagePacket(Packet packet) {
        super(packet);
        this.player = this.readByte();
        this.dmg = this.readInt();
    }
}
