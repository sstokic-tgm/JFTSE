package com.jftse.emulator.server.game.core.packet.packets.authserver;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CAuthLoginPacket extends Packet {
    public S2CAuthLoginPacket(char result, byte unk0) {
        super(PacketID.S2CAuthLoginData);
        this.write(result);
        this.write(unk0);
    }
}
