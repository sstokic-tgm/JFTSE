package com.jftse.server.core.shared.packets;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CWelcomePacket extends Packet {
    public S2CWelcomePacket(int decKey, int encKey, int decTblIdx, int encTblIdx) {
        super(PacketOperations.S2CLoginWelcomePacket.getValue());

        this.write(decKey);
        this.write(encKey);
        this.write(decTblIdx);
        this.write(encTblIdx);
    }
}
