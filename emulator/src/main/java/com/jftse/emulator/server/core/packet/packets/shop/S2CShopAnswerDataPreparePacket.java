package com.jftse.emulator.server.core.packet.packets.shop;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CShopAnswerDataPreparePacket extends Packet {
    public S2CShopAnswerDataPreparePacket(byte category, byte part, byte player, int size) {
        super(PacketOperations.S2CShopAnswerDataPrepare.getValueAsChar());

        this.write(category);
        this.write(part);
        this.write(player);
        this.write(size);
    }
}
