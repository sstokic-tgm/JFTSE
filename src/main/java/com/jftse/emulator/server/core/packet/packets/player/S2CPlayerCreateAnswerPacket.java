package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerCreateAnswerPacket extends Packet {
    public S2CPlayerCreateAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerCreateAnswer.getValueAsChar());

        this.write(result);
    }
}
