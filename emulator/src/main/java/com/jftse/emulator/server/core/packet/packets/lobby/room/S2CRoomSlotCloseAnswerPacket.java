package com.jftse.emulator.server.core.packet.packets.lobby.room;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CRoomSlotCloseAnswerPacket extends Packet {
    public S2CRoomSlotCloseAnswerPacket(byte slot, boolean deactivate) {
        super(PacketOperations.S2CRoomSlotCloseAnswer.getValueAsChar());

        this.write(slot);
        this.write(deactivate);
    }
}