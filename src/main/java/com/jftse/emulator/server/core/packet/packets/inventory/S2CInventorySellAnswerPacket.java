package com.jftse.emulator.server.core.packet.packets.inventory;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CInventorySellAnswerPacket extends Packet {
    public final static byte SUCCESS = 0;
    public final static byte NO_ITEM = -1;
    public final static byte IMPOSSIBLE_ITEM = -2;

    public S2CInventorySellAnswerPacket(byte status, int itemPocketId, int price) {
        super(PacketOperations.S2CInventorySellAnswer.getValueAsChar());

        this.write(status);
        this.write(itemPocketId);
        this.write(price);
    }
}
