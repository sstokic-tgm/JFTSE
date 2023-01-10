package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildCastleInfoAnswerPacket extends Packet {
    public S2CGuildCastleInfoAnswerPacket(byte unknown1, int unknown2, int unknown3,
                                          byte accessLimit, int admissionFee) {
        super(PacketOperations.S2CGuildCastleInfoAnswer);

        this.write(unknown1);
        this.write(unknown2);
        this.write(unknown3);
        this.write(accessLimit);
        this.write(admissionFee);
    }
}