package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.networking.packet.Packet;
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
