package com.jftse.emulator.server.core.packet.packets.home;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CHomeItemsRemoveAnswerPacket extends Packet {
    public S2CHomeItemsRemoveAnswerPacket(short result) {
        super(PacketID.S2CHomeItemsRemoveAnswer);

        this.write(result);
    }
}