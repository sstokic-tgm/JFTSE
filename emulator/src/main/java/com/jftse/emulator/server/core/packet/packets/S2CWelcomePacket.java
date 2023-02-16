package com.jftse.emulator.server.core.packet.packets;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CWelcomePacket extends Packet {
    public S2CWelcomePacket(int decKey, int encKey, int decTblIdx, int encTblIdx) {
        super(PacketOperations.S2CLoginWelcomePacket.getValueAsChar());

        this.write(decKey);
        this.write(encKey);
        this.write(decTblIdx);
        this.write(encTblIdx);
    }
}
