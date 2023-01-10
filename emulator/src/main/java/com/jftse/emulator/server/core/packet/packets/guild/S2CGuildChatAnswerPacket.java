package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGuildChatAnswerPacket extends Packet {
    public S2CGuildChatAnswerPacket(String playerName, String message) {
        super(PacketOperations.S2CGuildChatAnswer.getValueAsChar());

        this.write(playerName, message);
    }
}
