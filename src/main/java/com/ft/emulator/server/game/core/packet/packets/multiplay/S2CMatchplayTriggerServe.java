package com.ft.emulator.server.game.core.packet.packets.multiplay;

import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CMatchplayTriggerServe extends Packet {
    public S2CMatchplayTriggerServe(RoomPlayer roomPlayer) {
        super(PacketID.S2CMatchplayStartServe);
        this.write((char) 1);
        if(roomPlayer.isMaster()) {
            this.write((char)0);
            this.write(0);
            this.write(0);
            this.write((byte)1);
        }
        else {
            this.write((char)1);
            this.write(1);
            this.write(1);
            this.write((byte)0);
        }
    }
}