package com.jftse.emulator.server.core.packets.challenge;

import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CChallengeProgressAnswerPacket extends Packet {
    public S2CChallengeProgressAnswerPacket(List<ChallengeProgress> challengeProgressList) {
        super(PacketOperations.S2CChallengeProgressAck);

        this.write((char) challengeProgressList.size());
        challengeProgressList.forEach(cpl -> this.write((short) cpl.getChallenge().getChallengeIndex().intValue(), (short) cpl.getSuccess().intValue(), (short) cpl.getAttempts().intValue()));
    }
}
