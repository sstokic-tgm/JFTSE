package com.jftse.emulator.server.game.core.packet.packets.ranking;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRankingDataRequestPacket extends Packet {
    private byte gameMode;
    private int page;

    public C2SRankingDataRequestPacket(Packet packet) {
        super(packet);

        this.readInt(); // unk
        this.readByte(); // unk
        this.gameMode = this.readByte();
        this.page = this.readInt();
    }
}
