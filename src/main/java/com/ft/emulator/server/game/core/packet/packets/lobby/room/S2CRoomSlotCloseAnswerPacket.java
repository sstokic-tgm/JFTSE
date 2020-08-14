package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CRoomSlotCloseAnswerPacket extends Packet {
    public S2CRoomSlotCloseAnswerPacket(byte slot, boolean deactivate) {
        super(PacketID.S2CRoomSlotCloseAnswer);

        this.write(slot);
        this.write(deactivate);
    }
}