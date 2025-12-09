package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSendParcelRequestPacket extends Packet {
    private String receiverName;
    private String message;
    private Integer playerPocketId;
    private Integer cashOnDelivery;
    private Byte unk0;

    public C2SSendParcelRequestPacket(Packet packet) {
        super(packet);

        this.receiverName = this.readString();
        this.message = this.readString();
        this.playerPocketId = this.readInt();
        this.unk0 = this.readByte();
        this.cashOnDelivery = this.readInt();
    }
}
