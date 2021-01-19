package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerServe extends Packet {
    public S2CMatchplayTriggerServe(short position, float xPosition, float yPosition, boolean serveBall) {
        super(PacketID.S2CMatchplayStartServe);

        this.write((char) 1);
        this.write(position);
        this.write(xPosition);
        this.write(yPosition);
        this.write(serveBall);
    }
}