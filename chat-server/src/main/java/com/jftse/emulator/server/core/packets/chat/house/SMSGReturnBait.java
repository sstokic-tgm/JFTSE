package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class SMSGReturnBait extends Packet {
    public SMSGReturnBait(short playerPosition) {
        super(PacketOperations.SMSG_ReturnBait);

        this.write(playerPosition);
    }
}
