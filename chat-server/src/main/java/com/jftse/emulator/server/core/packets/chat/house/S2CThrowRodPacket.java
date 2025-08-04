package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CThrowRodPacket extends Packet {
    public S2CThrowRodPacket(short playerPosition, byte displayMessage) {
        super(PacketOperations.SMSG_ThrowBait);

        this.write(playerPosition);
        this.write(displayMessage);
    }
}
