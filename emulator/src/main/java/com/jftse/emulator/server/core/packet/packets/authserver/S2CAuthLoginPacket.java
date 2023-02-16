package com.jftse.emulator.server.core.packet.packets.authserver;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CAuthLoginPacket extends Packet {
    public S2CAuthLoginPacket(char result, byte unk0) {
        super(PacketOperations.S2CAuthLoginData.getValueAsChar());
        this.write(result);
        this.write(unk0);
    }
}
