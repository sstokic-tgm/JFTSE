package com.ft.emulator.server.game.core.packet.packets.chat;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CWhisperAnswerPacket extends Packet {
    public S2CWhisperAnswerPacket(String senderName, String receiverName, String message) {
        super(PacketID.S2CWhisperAnswer);

        this.write(senderName, receiverName, message);
    }
}
