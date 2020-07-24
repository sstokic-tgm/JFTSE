package com.ft.emulator.server.game.core.packet.packets.challenge;

import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CChallengeProgressAnswerPacket extends Packet {

    public S2CChallengeProgressAnswerPacket(List<ChallengeProgress> challengeProgressList) {

	super(PacketID.S2CChallengeProgressAck);

	this.write((char) challengeProgressList.size());
	challengeProgressList.forEach(cpl -> this.write((short) cpl.getChallenge().getChallengeIndex().intValue(), (short) cpl.getSuccess().intValue(), (short) cpl.getAttempts().intValue()));
    }
}