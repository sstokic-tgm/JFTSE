package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.networking.packet.Packet;
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

        this.receiverName = packet.readUnicodeString();
        this.message = packet.readUnicodeString();
        this.playerPocketId = packet.readInt();
        this.unk0 = packet.readByte();
        this.cashOnDelivery = packet.readInt();
    }
}
