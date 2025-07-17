package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInitFishWithDetailsPacket extends Packet {
    public S2CInitFishWithDetailsPacket(short fishId, byte fishModel, byte fishState, float unk0, float z, float unk1,
                                        float x, float y, float unk2, float scale, float unk3, float unk4, float speed,
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
        this.write(unk2);
        this.write(scale);
        this.write(unk3);
        this.write(unk4);
        this.write(speed);
        this.write(unk5);
        this.write(unk6);
        this.write(unk7);
    }
}
