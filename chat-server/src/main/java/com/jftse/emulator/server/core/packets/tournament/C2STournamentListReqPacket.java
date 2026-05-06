package com.jftse.emulator.server.core.packets.tournament;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2STournamentListReqPacket extends Packet {
    private int unk0;

    public C2STournamentListReqPacket(Packet packet) {
        super(packet);

        this.unk0 = this.readInt();
    }
}
