package com.ft.emulator.server.game.server.packets.challenge;

import com.ft.emulator.server.database.model.challenge.ChallengeProgress;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CChallengeProgressAnswerPacket extends Packet {

    public S2CChallengeProgressAnswerPacket(List<ChallengeProgress> challengeProgressList) {

        super(PacketID.S2CChallengeProgressAck);

        this.write((char)challengeProgressList.size());
        for(ChallengeProgress challengeProgress : challengeProgressList) {

            this.write((short)Math.toIntExact(challengeProgress.getChallenge().getChallengeIndex()));
            this.write((short)challengeProgress.getSuccess().intValue());
            this.write((short)challengeProgress.getAttempts().intValue());
	}
    }
}