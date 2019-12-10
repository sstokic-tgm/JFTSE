package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SFirstCharacterPacket extends Packet {

    private byte characterType;

    public C2SFirstCharacterPacket(Packet packet) {

        super(packet);

        this.characterType = this.readByte();
    }
}