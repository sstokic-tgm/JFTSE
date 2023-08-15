package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CChatLobbyAnswerPacket extends Packet {
    public S2CChatLobbyAnswerPacket(char unk, String name, String message) {
        super(PacketOperations.S2CChatLobbyAnswer);

        this.write(unk, name, message);
    }
}
