package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventorySellItemCheckAnswerPacket extends Packet {
    public final static byte SUCCESS = 0;
    public final static byte NO_ITEM = -1;
    public final static byte IMPOSSIBLE_ITEM = -2;

    public S2CInventorySellItemCheckAnswerPacket(byte status) {
        super(PacketOperations.S2CInventorySellItemCheckAnswer.getValueAsChar());

        this.write(status);
    }
}
