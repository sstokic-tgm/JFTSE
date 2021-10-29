package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SProposalListRequestPacket extends Packet {
    private byte listType;

    public C2SProposalListRequestPacket(Packet packet) {
        super(packet);

        this.listType = this.readByte();
        this.readInt(); // unk
    }
}
