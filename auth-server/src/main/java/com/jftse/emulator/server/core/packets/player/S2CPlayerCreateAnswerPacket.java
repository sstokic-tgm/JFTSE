package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPlayerCreateAnswerPacket extends Packet {
    public S2CPlayerCreateAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerCreateAnswer.getValue());

        this.write(result);
    }
}
