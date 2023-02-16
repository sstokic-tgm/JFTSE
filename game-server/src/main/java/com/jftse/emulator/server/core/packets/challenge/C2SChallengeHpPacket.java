package com.jftse.emulator.server.core.packets.challenge;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengeHpPacket extends Packet {

    private char playerHp;
    private char npcHp;

    public C2SChallengeHpPacket(Packet packet) {
        super(packet);

        this.playerHp = this.readChar();
        this.npcHp = this.readChar();
    }
}
