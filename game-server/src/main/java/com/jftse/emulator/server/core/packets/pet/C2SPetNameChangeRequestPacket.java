package com.jftse.emulator.server.core.packets.pet;

import com.jftse.server.core.protocol.Packet;
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
