package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPetPickupRequestPacket extends Packet {
    private Integer petType;

    public C2SPetPickupRequestPacket(Packet packet) {
        super(packet);

        this.petType = packet.readInt();
    }
}
