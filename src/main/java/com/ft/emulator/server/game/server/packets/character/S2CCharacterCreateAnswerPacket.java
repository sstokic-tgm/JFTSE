package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterCreateAnswerPacket extends Packet {

    public S2CCharacterCreateAnswerPacket(char result) {

        super(PacketID.S2CCharacterCreateAnswer);

        this.write(result);
    }
}