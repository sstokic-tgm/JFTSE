package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildLeaveAnswerPacket extends Packet {
    public S2CGuildLeaveAnswerPacket(char status) {
        super(PacketID.S2CGuildLeaveAnswer);

        this.write(status);
    }
}
