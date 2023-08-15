package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CChatRoomAnswerPacket extends Packet {
    public S2CChatRoomAnswerPacket(byte type, String name, String message) {
        super(PacketOperations.S2CChatRoomAnswer);

        this.write(type, name, message);
    }
}
