package com.ft.emulator.server.game.core.packet.packets.shop;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CShopAnswerDataPreparePacket extends Packet {
    public S2CShopAnswerDataPreparePacket(byte category, byte part, byte player, int size) {
        super(PacketID.S2CShopAnswerDataPrepare);

        this.write(category);
        this.write(part);
        this.write(player);
        this.write(size);
    }
}
