package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CCharacterInfoPlayStatsPacket extends Packet {

    public S2CCharacterInfoPlayStatsPacket() {

        super(PacketID.S2CCharacterInfoPlayStatsData);

	this.write(0); // basic record win
	this.write(0); // basic record loss
	this.write(0); // battle record win
	this.write(0); // battle record loss

	this.write(0); // consecutive wins
	this.write(0);
	this.write(0);
	this.write(0);
	this.write(0);
	this.write(0);
    }
}