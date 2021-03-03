package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SMatchplayPlayerIdsInSessionPacket extends Packet {
    private int sessionId;
    private byte unk0;
    private List<Integer> playerIds;

    public C2SMatchplayPlayerIdsInSessionPacket(Packet packet) {
        super(packet);

        this.playerIds = new ArrayList<>();

        this.sessionId = this.readInt();
        this.unk0 = this.readByte();
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
    }
}
