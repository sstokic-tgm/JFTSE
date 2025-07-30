package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInitFishWithDetailsPacket extends Packet {
    public S2CInitFishWithDetailsPacket(short fishId, byte fishModel, byte fishState, float unk0, float z, float unk1,
                                        float x, float y, float dirX, float dirY, float destX, float destY, float speed,
                                        float unk5, float unk6, short unk7) {
        super(PacketOperations.S2CInitFishWithDetails);

        this.write(fishId);
        this.write(fishModel);
        this.write(fishState);
        this.write(unk0);
        this.write(z);
        this.write(unk1);
        this.write(x);
        this.write(y);
        this.write(dirX);
        this.write(dirY);
        this.write(destX);
        this.write(destY);
        this.write(speed);
        this.write(unk5);
        this.write(unk6);
        this.write(unk7);
    }
}
