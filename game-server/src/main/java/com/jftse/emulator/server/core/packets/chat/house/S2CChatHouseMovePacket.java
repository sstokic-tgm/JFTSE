package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CChatHouseMovePacket extends Packet {
    public S2CChatHouseMovePacket(short position, byte unk1, byte unk2, short x, short y, byte animationType, byte unk3) {
        super(PacketOperations.S2CChatHouseMove);

        this.write(position);
        this.write(unk1);
        this.write(unk2);
        this.write(x);
        this.write(y);
        this.write(animationType);
        this.write(unk3);
    }
}
