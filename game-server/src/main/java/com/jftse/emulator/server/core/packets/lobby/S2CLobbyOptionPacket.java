package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CLobbyOptionPacket extends Packet {
    public S2CLobbyOptionPacket(byte option) {
        super(PacketOperations.S2CLobbyOption);

        this.write(option);
    }
}
