package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerNameCheckPacket extends Packet {

    private String nickname;

    public C2SPlayerNameCheckPacket(Packet packet) {

        super(packet);

        this.nickname = this.readUnicodeString();
        this.nickname = getNickname().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
    }
}