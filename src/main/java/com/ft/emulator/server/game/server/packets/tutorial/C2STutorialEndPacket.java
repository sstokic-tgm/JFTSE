package com.ft.emulator.server.game.server.packets.tutorial;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2STutorialEndPacket extends Packet {

    private byte tutorialId;

    public C2STutorialEndPacket(Packet packet) {

        super(packet);

        this.tutorialId = this.readByte();
    }
}