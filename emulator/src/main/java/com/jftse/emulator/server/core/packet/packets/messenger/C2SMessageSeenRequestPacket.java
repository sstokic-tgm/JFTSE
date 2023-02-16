package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
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
