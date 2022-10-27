package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CInventorySellItemCheckAnswerPacket extends Packet {
    public final static byte SUCCESS = 0;
    public final static byte NO_ITEM = -1;
    public final static byte IMPOSSIBLE_ITEM = -2;

    public S2CInventorySellItemCheckAnswerPacket(byte status) {
        super(PacketOperations.S2CInventorySellItemCheckAnswer.getValue());

        this.write(status);
    }
}
