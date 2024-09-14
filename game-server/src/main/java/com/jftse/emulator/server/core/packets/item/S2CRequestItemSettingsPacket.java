package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRequestItemSettingsPacket extends Packet {
    public S2CRequestItemSettingsPacket() {
        super(PacketOperations.S2CRequestItemSettings);
    }
}
