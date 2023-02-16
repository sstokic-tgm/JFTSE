package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSlotCloseAnswerPacket extends Packet {
    public S2CRoomSlotCloseAnswerPacket(byte slot, boolean deactivate) {
        super(PacketOperations.S2CRoomSlotCloseAnswer);

        this.write(slot);
        this.write(deactivate);
    }
}