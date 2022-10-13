package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPlayerNameCheckAnswerPacket extends Packet {
    public S2CPlayerNameCheckAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerNameCheckAnswer.getValue());

        this.write(result);
    }
}
