package com.jftse.server.core.shared.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CServerNoticePacket extends Packet {
    public S2CServerNoticePacket(String message) {
        super(PacketOperations.S2CServerNotice);

        this.write(message);
    }
}