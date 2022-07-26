package com.jftse.emulator.server.core.packet.packets.chat;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CChatRoomAnswerPacket extends Packet {
    public S2CChatRoomAnswerPacket(byte type, String name, String message) {
        super(PacketOperations.S2CChatRoomAnswer.getValueAsChar());

        this.write(type, name, message);
    }
}
