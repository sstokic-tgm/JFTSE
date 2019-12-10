package com.ft.emulator.server.game.server.packets.chat;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CWhisperAnswerPacket extends Packet {

    public S2CWhisperAnswerPacket(String senderName, String receiverName, String message) {

        super(PacketID.S2CWhisperAnswer);

        this.write(senderName);
        this.write((char)0);
        this.write(receiverName);
        this.write((char)0);
        this.write(message);
        this.write((char)0);
    }
}