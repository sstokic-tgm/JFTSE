package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
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

        this.readByte(); //Unk
        this.receiverName = this.readString();
        this.message = this.readString();
        this.productIndex = this.readInt();
        this.option = this.readByte();
    }
}
