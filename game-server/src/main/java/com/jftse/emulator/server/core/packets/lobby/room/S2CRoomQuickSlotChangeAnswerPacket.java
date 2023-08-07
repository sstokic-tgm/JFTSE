package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomQuickSlotChangeAnswerPacket extends Packet {
    public S2CRoomQuickSlotChangeAnswerPacket(boolean isQuickSlot) {
        super(PacketOperations.S2CRoomQuickSlotChange);

        this.write(isQuickSlot);
    }
}
