package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CShakeTreeFailPacket extends Packet {
    public S2CShakeTreeFailPacket(short position, FruitTree fruitTree, short unk1) {
        super(PacketOperations.S2CShakeTreeFail);

        this.write(position);
        this.write(fruitTree.getX());
        this.write(fruitTree.getY());
        this.write(unk1);
    }

    public S2CShakeTreeFailPacket(short position, short x, short y, short unk1) {
        super(PacketOperations.S2CShakeTreeFail);

        this.write(position);
        this.write(x);
        this.write(y);
        this.write(unk1);
    }
}
