package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SMessageSeenRequestPacket extends Packet {
    private Byte type;
    private Integer messageId;

    public C2SMessageSeenRequestPacket(Packet packet) {
        super(packet);

        this.type = packet.readByte();
        this.messageId = packet.readInt();
    }
}
