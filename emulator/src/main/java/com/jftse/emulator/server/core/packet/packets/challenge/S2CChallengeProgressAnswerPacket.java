package com.jftse.emulator.server.core.packet.packets.challenge;

import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CChallengeProgressAnswerPacket extends Packet {
    public S2CChallengeProgressAnswerPacket(List<ChallengeProgress> challengeProgressList) {
        super(PacketOperations.S2CChallengeProgressAck.getValueAsChar());

        this.write((char) challengeProgressList.size());
        challengeProgressList.forEach(cpl -> this.write((short) cpl.getChallenge().getChallengeIndex().intValue(), (short) cpl.getSuccess().intValue(), (short) cpl.getAttempts().intValue()));
    }
}
