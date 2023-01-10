package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
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
