package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CShakeTreeSuccessPacket extends Packet {
    public S2CShakeTreeSuccessPacket(short position, FruitTree fruitTree, short unk1) {
        super(PacketOperations.S2CShakeTreeSuccess);

        this.write(position);
        this.write(fruitTree.getX());
        this.write(fruitTree.getY());
        this.write(unk1);
    }
}
