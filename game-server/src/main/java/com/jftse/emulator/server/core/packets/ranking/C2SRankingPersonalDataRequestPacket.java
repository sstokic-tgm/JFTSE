package com.jftse.emulator.server.core.packets.ranking;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRankingPersonalDataRequestPacket extends Packet {
    private byte gameMode;
    private String nickname;

    public C2SRankingPersonalDataRequestPacket(Packet packet) {
        super(packet);

        this.readInt(); // unk
        this.readByte(); // unk;
        this.gameMode = this.readByte();
        this.nickname = this.readUnicodeString();
    }
}
