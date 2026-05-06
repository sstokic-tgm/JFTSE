package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.Map;

public class S2CPersonalBoardMessageListPacket extends Packet {
    public S2CPersonalBoardMessageListPacket(Map<Integer, String> messages) {
        super(PacketOperations.S2CPersonalBoardMessageList);

        this.write((short) messages.size());
        for (Map.Entry<Integer, String> entry : messages.entrySet()) {
            this.write(entry.getKey().shortValue());
            this.write(entry.getValue());
        }
    }
}
