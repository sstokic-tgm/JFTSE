package com.jftse.emulator.server.core.packet.packets.pet;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPetNameChangeRequestPacket extends Packet {
    private Integer itemId;
    private Byte petType;
    private String newPetName;

    public C2SPetNameChangeRequestPacket(Packet packet) {
        super(packet);

        this.itemId = packet.readInt();
        this.petType = packet.readByte();
        this.newPetName = packet.readUnicodeString();
    }
}
