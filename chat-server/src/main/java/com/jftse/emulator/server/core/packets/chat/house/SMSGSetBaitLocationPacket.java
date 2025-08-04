package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class SMSGSetBaitLocationPacket extends Packet {
    public SMSGSetBaitLocationPacket(short playerPosition, float x, float z, float y) {
        super(PacketOperations.SMSG_SetBaitLocation);

        this.write(playerPosition);
        this.write(x);
        this.write(z);
        this.write(y);
    }
}
