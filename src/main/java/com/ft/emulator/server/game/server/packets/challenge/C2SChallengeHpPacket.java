package com.ft.emulator.server.game.server.packets.challenge;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChallengeHpPacket extends Packet {

    private char npcHp;
    private char playerHp;

    public C2SChallengeHpPacket(Packet packet) {

        super(packet);

        this.npcHp = this.readChar();
        this.playerHp = this.readChar();
    }
}