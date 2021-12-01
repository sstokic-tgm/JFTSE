package com.jftse.emulator.server.core.packet.packets.chat;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CWhisperAnswerPacket extends Packet {
    public S2CWhisperAnswerPacket(String senderName, String receiverName, String message) {
        super(PacketOperations.S2CWhisperAnswer.getValueAsChar());

        this.write(senderName, receiverName, message);
    }
}
