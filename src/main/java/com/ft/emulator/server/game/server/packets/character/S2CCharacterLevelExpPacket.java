package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterLevelExpPacket extends Packet {

    public S2CCharacterLevelExpPacket(byte level, int expValue) {

        super(PacketID.S2CCharacterLevelExpData);

        this.write(level);
        this.write(expValue);
    }
}