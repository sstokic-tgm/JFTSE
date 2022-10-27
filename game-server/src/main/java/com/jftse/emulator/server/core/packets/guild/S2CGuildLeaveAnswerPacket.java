package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildLeaveAnswerPacket extends Packet {
    public S2CGuildLeaveAnswerPacket(char status) {
        super(PacketOperations.S2CGuildLeaveAnswer.getValue());

        this.write(status);
    }
}
