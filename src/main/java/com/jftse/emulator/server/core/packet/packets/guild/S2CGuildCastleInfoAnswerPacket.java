package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildCastleInfoAnswerPacket extends Packet {
    public S2CGuildCastleInfoAnswerPacket(byte unknown1, int unknown2, int unknown3,
                                          byte accessLimit, int admissionFee) {
        super(PacketID.S2CGuildCastleInfoAnswer);

        this.write(unknown1);
        this.write(unknown2);
        this.write(unknown3);
        this.write(accessLimit);
        this.write(admissionFee);
    }
}