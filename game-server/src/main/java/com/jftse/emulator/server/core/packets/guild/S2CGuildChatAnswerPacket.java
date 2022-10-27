package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGuildChatAnswerPacket extends Packet {
    public S2CGuildChatAnswerPacket(String playerName, String message) {
        super(PacketOperations.S2CGuildChatAnswer.getValue());

        this.write(playerName, message);
    }
}
