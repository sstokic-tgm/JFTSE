package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterDeleteAnswerPacket extends Packet {

    public S2CCharacterDeleteAnswerPacket(char result) {

        super(PacketID.S2CCharacterDelete);

        this.write(result);
    }
}