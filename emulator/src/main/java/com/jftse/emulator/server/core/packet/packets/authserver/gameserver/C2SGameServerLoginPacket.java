package com.jftse.emulator.server.core.packet.packets.authserver.gameserver;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.nio.charset.StandardCharsets;

@Getter
@Setter
public class C2SGameServerLoginPacket extends Packet {
    private Long playerId;
    private String token;
    private Long timestamp;
    private String accountName;
    private Byte unk0;
    private String hwid;

    public C2SGameServerLoginPacket(Packet packet) {
        super(packet);
        this.playerId = (long) this.readInt();
        byte[] tokenBytes = new byte[16];
        for (int i = 0; i < 16; i++)
            tokenBytes[i] = this.readByte();
        this.token = new String(tokenBytes, StandardCharsets.UTF_8);

        this.timestamp = this.readLong();
        this.accountName = this.readUnicodeString();
        this.unk0 = this.readByte();
        this.hwid = this.readString();

        this.accountName = getAccountName().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
    }
}
