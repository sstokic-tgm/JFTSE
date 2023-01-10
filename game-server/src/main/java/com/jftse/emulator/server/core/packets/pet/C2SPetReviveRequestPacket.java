package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPetReviveRequestPacket extends Packet {
    private Integer itemId;
    private Byte petType;

    public C2SPetReviveRequestPacket(Packet packet) {
        super(packet);

        this.itemId = packet.readInt();
        this.petType = packet.readByte();
    }
}
