package com.ft.emulator.server.game.server.packets.authserver;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLoginPacket extends Packet {

    private String username;
    private String password;

    public C2SLoginPacket(Packet packet) {

        super(packet);

        this.username = this.readUnicodeString();
        this.password = this.readString();

        this.username = getUsername().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
        this.password = getPassword().trim();
    }
}