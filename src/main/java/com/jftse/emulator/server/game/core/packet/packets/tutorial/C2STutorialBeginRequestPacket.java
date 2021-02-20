package com.jftse.emulator.server.game.core.packet.packets.tutorial;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2STutorialBeginRequestPacket extends Packet {
    private char tutorialId;

    public C2STutorialBeginRequestPacket(Packet packet) {
        super(packet);

        this.tutorialId = this.readChar();
    }
}
