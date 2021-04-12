package com.jftse.emulator.server.game.core.packet.packets.authserver;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLoginPacket extends Packet {
    private String username;
    private String password;
    private int version;
    private String hwid;

    public C2SLoginPacket(Packet packet) {
        super(packet);
        this.username = this.readUnicodeString();
        this.password = this.readString();
        this.version = this.readInt();
        this.readByte();
        this.hwid = this.readString();

        this.username = getUsername().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
        this.password = getPassword().trim();
    }
}
