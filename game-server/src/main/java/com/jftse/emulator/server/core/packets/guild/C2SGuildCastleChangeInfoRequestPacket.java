package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildCastleChangeInfoRequestPacket extends Packet {
    private int unknown1;
    private int unknown2;
    private byte accessLimit;
    private int admissionFee;

    public C2SGuildCastleChangeInfoRequestPacket(Packet packet) {
        super(packet);

        this.unknown1 = this.readInt();
        this.unknown2 = this.readInt();
        this.accessLimit = this.readByte();
        this.admissionFee = this.readInt();
    }
}