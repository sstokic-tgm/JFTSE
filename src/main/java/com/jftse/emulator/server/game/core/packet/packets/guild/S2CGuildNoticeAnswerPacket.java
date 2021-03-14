package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildNoticeAnswerPacket extends Packet {
    public S2CGuildNoticeAnswerPacket(String notice) {
        super(PacketID.S2CGuildNoticeAnswer);

        this.write(notice);
    }
}
