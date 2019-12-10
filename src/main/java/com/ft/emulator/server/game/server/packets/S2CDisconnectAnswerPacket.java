package com.ft.emulator.server.game.server.packets;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CDisconnectAnswerPacket extends Packet {

    public S2CDisconnectAnswerPacket() {

        super(PacketID.S2CDisconnectAnswer);

        this.write((byte)0);
    }
}