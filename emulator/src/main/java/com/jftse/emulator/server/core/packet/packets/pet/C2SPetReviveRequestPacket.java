package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.networking.packet.Packet;
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
