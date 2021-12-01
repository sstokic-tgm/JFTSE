package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildNoticeAnswerPacket extends Packet {
    public S2CGuildNoticeAnswerPacket(String notice) {
        super(PacketOperations.S2CGuildNoticeAnswer.getValueAsChar());

        this.write(notice);
    }
}
