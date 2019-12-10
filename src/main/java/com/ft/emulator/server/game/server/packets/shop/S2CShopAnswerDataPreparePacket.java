package com.ft.emulator.server.game.server.packets.shop;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CShopAnswerDataPreparePacket extends Packet {

    public S2CShopAnswerDataPreparePacket(byte category, byte part, byte character, int size) {

        super(PacketID.S2CShopAnswerDataPrepare);

        this.write(category);
        this.write(part);
        this.write(character);
        this.write(size);
    }
}