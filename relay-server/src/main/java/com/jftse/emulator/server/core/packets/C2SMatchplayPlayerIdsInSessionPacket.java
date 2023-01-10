package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SMatchplayPlayerIdsInSessionPacket extends Packet {
    private int sessionId;
    private boolean isSpectator;
    private List<Integer> playerIds;

    public C2SMatchplayPlayerIdsInSessionPacket(Packet packet) {
        super(packet);

        this.playerIds = new ArrayList<>();

        this.sessionId = this.readInt();
        this.isSpectator = this.readBoolean();
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
        playerIds.add(this.readInt());
    }
}
