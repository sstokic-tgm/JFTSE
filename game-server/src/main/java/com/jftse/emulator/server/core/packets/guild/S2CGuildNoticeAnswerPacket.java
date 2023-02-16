package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildNoticeAnswerPacket extends Packet {
    public S2CGuildNoticeAnswerPacket(String notice) {
        super(PacketOperations.S2CGuildNoticeAnswer);

        this.write(notice);
    }
}
