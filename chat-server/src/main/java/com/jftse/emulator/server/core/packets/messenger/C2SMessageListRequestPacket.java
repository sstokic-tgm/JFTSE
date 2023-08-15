package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMessageListRequestPacket extends Packet {
    private byte listType;

    public C2SMessageListRequestPacket(Packet packet) {
        super(packet);

        this.listType = this.readByte();
        this.readInt(); // unk
    }
}
