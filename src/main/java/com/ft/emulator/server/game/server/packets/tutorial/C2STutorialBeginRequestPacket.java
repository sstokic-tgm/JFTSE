package com.ft.emulator.server.game.server.packets.tutorial;

import com.ft.emulator.server.game.server.Packet;
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