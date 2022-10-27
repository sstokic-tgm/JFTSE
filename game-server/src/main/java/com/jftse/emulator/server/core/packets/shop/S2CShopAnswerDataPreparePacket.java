package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CShopAnswerDataPreparePacket extends Packet {
    public S2CShopAnswerDataPreparePacket(byte category, byte part, byte player, int size) {
        super(PacketOperations.S2CShopAnswerDataPrepare.getValue());

        this.write(category);
        this.write(part);
        this.write(player);
        this.write(size);
    }
}
