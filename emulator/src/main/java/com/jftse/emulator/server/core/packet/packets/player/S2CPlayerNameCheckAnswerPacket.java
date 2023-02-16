package com.jftse.emulator.server.core.packet.packets.player;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CPlayerNameCheckAnswerPacket extends Packet {
    public S2CPlayerNameCheckAnswerPacket(char result) {
        super(PacketOperations.S2CPlayerNameCheckAnswer.getValueAsChar());

        this.write(result);
    }
}
