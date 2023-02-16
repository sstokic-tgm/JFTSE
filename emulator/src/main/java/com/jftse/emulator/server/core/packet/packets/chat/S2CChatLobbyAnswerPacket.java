package com.jftse.emulator.server.core.packet.packets.chat;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CChatLobbyAnswerPacket extends Packet {
    public S2CChatLobbyAnswerPacket(char unk, String name, String message) {
        super(PacketOperations.S2CChatLobbyAnswer.getValueAsChar());

        this.write(unk, name, message);
    }
}
