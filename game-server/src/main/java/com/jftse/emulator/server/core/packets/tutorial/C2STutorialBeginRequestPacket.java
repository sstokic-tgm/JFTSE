package com.jftse.emulator.server.core.packets.tutorial;

import com.jftse.server.core.protocol.Packet;
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
