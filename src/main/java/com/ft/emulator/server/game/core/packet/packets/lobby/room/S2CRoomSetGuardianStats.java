package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CRoomSetGuardianStats extends Packet {
    public S2CRoomSetGuardianStats(byte amountOfGuardians, short guardianHealth) {
        super(PacketID.S2CRoomSetGuardianStats);

        this.write(amountOfGuardians);
        for (byte i = 0; i < amountOfGuardians; i++)
        {
            this.write(i); // GuardianPosition
            this.write(guardianHealth); // HP
            this.write((byte)10); // STR?
            this.write((byte)10); // STA?
            this.write((byte)10); // DEX?
            this.write((byte)10); // WIL?
        }
    }
}