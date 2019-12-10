package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CFirstCharacterAnswerPacket extends Packet {

    public S2CFirstCharacterAnswerPacket(char result, Long characterId, byte characterType ) {

        super(PacketID.S2CLoginFirstCharacterAnswer);

        this.write(result);
        this.write(Math.toIntExact(characterId));
        this.write(characterType);
    }
}