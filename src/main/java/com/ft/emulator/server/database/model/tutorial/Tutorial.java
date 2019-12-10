package com.ft.emulator.server.database.model.tutorial;

import com.ft.emulator.server.database.model.challenge.ChallengeReward;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Tutorial extends ChallengeReward {

    @Column(unique = true)
    private Long tutorialIndex;
}