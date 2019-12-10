package com.ft.emulator.server.game.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class ChallengeBattleGame extends ChallengeGame {

    private Integer playerHp;
    private Integer npcHp;

    public ChallengeBattleGame(Long challengeId) {

        this.setChallengeId(challengeId);

	Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	this.setStartTime(currentCalender.getTime());

	this.playerHp = 0;
	this.npcHp = 0;

	this.setFinished(false);
    }

    public void setHp(byte player, int hp) {

        if(player == 1) {
            this.playerHp += hp;
	}
        else {
            this.npcHp += hp;
	}

        if(this.playerHp <= 0 || this.npcHp <= 0) {

	    this.setFinished(true);

	    Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	    this.setEndTime(currentCalender.getTime());
	}
    }

    @Override
    public long getTimeNeeded() {
	return getEndTime().getTime() - getStartTime().getTime();
    }
}