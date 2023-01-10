package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CInventorySellAnswerPacket extends Packet {
    public final static byte SUCCESS = 0;
    public final static byte NO_ITEM = -1;
    public final static byte IMPOSSIBLE_ITEM = -2;

    public S2CInventorySellAnswerPacket(byte status, int itemPocketId, int price) {
        super(PacketOperations.S2CInventorySellAnswer);

        this.write(status);
        this.write(itemPocketId);
        this.write(price);
    }
}
