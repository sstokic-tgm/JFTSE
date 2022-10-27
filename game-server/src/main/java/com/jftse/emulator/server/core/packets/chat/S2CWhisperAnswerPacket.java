package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CWhisperAnswerPacket extends Packet {
    public S2CWhisperAnswerPacket(String senderName, String receiverName, String message) {
        super(PacketOperations.S2CWhisperAnswer.getValue());

        this.write(senderName, receiverName, message);
    }
}
