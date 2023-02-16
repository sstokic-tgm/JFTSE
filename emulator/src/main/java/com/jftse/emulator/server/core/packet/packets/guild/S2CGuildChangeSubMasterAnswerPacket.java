package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeSubMasterAnswerPacket extends Packet {
    public S2CGuildChangeSubMasterAnswerPacket(byte status, short result) {
        super(PacketOperations.S2CGuildChangeSubMasterAnswer.getValueAsChar());

        this.write(status);
        this.write(result);
    }
}
