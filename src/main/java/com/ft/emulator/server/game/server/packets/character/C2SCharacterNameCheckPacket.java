package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SCharacterNameCheckPacket extends Packet {

    private String nickname;

    public C2SCharacterNameCheckPacket(Packet packet) {

        super(packet);

        this.nickname = this.readUnicodeString();
        this.nickname = getNickname().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
    }
}