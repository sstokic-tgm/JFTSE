package com.ft.emulator.server.game.core.packet.packets.player;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CPlayerInfoPlayStatsPacket extends Packet {

    public S2CPlayerInfoPlayStatsPacket() {

	super(PacketID.S2CPlayerInfoPlayStatsData);

	this.write(0); // basic record win
	this.write(0); // basic record loss
	this.write(0); // battle record win
	this.write(0); // battle record loss

	this.write(0); // consecutive wins
	this.write(0);
	this.write(0); // number of disconnects
	this.write(0); // games played in sum
	this.write(0);
	this.write(0);
    }
}