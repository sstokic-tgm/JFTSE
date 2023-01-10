package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChangeSubMasterAnswerPacket extends Packet {
    public S2CGuildChangeSubMasterAnswerPacket(byte status, short result) {
        super(PacketOperations.S2CGuildChangeSubMasterAnswer);

        this.write(status);
        this.write(result);
    }
}
