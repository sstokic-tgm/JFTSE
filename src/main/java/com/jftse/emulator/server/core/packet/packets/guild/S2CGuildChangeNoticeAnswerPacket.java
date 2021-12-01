package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChangeNoticeAnswerPacket extends Packet {
    public S2CGuildChangeNoticeAnswerPacket(char result) {
        super(PacketOperations.S2CGuildChangeNoticeAnswer.getValueAsChar());

        this.write(result);
    }
}