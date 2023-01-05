package com.jftse.emulator.server.core.packets.authserver;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CAuthLoginPacket extends Packet {
    public S2CAuthLoginPacket(char result) {
        super(PacketOperations.S2CAuthLoginData);
        this.write(result);
    }
}
