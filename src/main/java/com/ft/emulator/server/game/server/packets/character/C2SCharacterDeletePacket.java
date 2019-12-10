package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SCharacterDeletePacket extends Packet {

    private int characterId;

    public C2SCharacterDeletePacket(Packet packet) {

        super(packet);

        this.characterId = this.readInt();
    }
}