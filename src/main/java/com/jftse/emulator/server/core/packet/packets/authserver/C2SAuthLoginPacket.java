package com.jftse.emulator.server.core.packet.packets.authserver;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class C2SAuthLoginPacket extends Packet {
    private String username;
    private String token;
    private Long timestamp;

    public C2SAuthLoginPacket(Packet packet) {
        super(packet);
        this.username = this.readUnicodeString();
        byte[] tokenBytes = new byte[16];
        for (int i = 0; i < 16; i++)
            tokenBytes[i] = this.readByte();
        this.token = new String(tokenBytes, StandardCharsets.UTF_8);

        this.timestamp = this.readLong();

        this.username = getUsername().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
    }
}
