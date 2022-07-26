package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSendGiftRequestPacket extends Packet {
    private String receiverName;
    private String message;
    private Integer productIndex;
    private Byte option;

    public C2SSendGiftRequestPacket(Packet packet) {
        super(packet);

        packet.readByte(); //Unk
        this.receiverName = packet.readUnicodeString();
        this.message = packet.readUnicodeString();
        this.productIndex = packet.readInt();
        this.option = packet.readByte();
    }
}
