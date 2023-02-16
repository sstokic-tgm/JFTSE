package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CPlayerDeleteAnswerPacket extends Packet {
    public S2CPlayerDeleteAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerDelete);

        this.write(result);
    }
}
