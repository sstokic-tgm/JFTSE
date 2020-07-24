package com.ft.emulator.server.game.core.packet.packets;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CDisconnectAnswerPacket extends Packet {

    public S2CDisconnectAnswerPacket() {

	super(PacketID.S2CDisconnectAnswer);

	this.write((byte) 0);
    }
}