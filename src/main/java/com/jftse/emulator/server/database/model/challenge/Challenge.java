package com.jftse.emulator.server.database.model.challenge;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Challenge extends ChallengeReward {
    @Column(unique = true)
    private Integer challengeIndex;

    private Short gameMode;

    private Byte level;
    private Byte levelRestriction;

    // NPC
    private Integer hp;
    private Byte str;
    private Byte sta;
    private Byte dex;
    private Byte wil;
}
