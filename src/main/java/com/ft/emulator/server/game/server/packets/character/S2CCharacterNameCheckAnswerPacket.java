package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterNameCheckAnswerPacket extends Packet {

    public S2CCharacterNameCheckAnswerPacket(char result) {

        super(PacketID.S2CCharacterNameCheckAnswer);

        this.write(result);
    }
}