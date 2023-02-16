package com.jftse.emulator.server.core.packet.packets;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CDCMsgPacket extends Packet {
    public S2CDCMsgPacket(Integer result) {
        super(PacketOperations.S2CDCMsg.getValueAsChar());

        this.write(result.shortValue());
    }
}
