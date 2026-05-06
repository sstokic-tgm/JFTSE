package com.jftse.server.core.shared.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CDCMsgPacket extends Packet {
    public S2CDCMsgPacket(Integer result) {
        super(PacketOperations.S2CDCMsg);

        this.write(result.byteValue());
    }
}
