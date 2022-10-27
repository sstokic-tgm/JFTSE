package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChangeNoticeAnswerPacket extends Packet {
    public S2CGuildChangeNoticeAnswerPacket(char result) {
        super(PacketOperations.S2CGuildChangeNoticeAnswer.getValue());

        this.write(result);
    }
}