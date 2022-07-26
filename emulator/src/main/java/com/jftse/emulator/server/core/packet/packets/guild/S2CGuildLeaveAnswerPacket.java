package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildLeaveAnswerPacket extends Packet {
    public S2CGuildLeaveAnswerPacket(char status) {
        super(PacketOperations.S2CGuildLeaveAnswer.getValueAsChar());

        this.write(status);
    }
}
