package com.jftse.emulator.server.game.core.packet.packets.ranking;

import com.jftse.emulator.server.networking.packet.Packet;
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
