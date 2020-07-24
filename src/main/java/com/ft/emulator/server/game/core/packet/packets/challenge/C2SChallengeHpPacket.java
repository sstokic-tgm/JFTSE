package com.ft.emulator.server.game.core.packet.packets.challenge;

import com.ft.emulator.server.networking.packet.Packet;
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