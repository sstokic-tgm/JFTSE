package com.ft.emulator.server.game.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class ChallengeBattleGame extends ChallengeGame {

    private Integer playerHp, maxPlayerHp;
    private Integer npcHp, maxNpcHp;

    public ChallengeBattleGame(Long challengeId) {

        this.setChallengeId(challengeId);

	Calendar currentCalender = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
	this.setStartTime(currentCalender.getTime());

	// max hp because if we heal, we don't want to exceed it
	this.maxPlayerHp = 0;
	this.playerHp = 0;
	this.maxNpcHp = 0;
	this.npcHp = 0;

	this.setFinished(false);
    }

    public void setHp(byte player, int hp) {

        if(player == 1) {

            this.playerHp += hp;
            if(this.playerHp > this.maxPlayerHp) {
                this.playerHp = this.maxPlayerHp;
	    }
	}
        else {

            this.npcHp += hp;
	    if(this.npcHp > this.maxNpcHp) {
		this.npcHp = this.maxNpcHp;
	    }
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

    public void setMaxPlayerHp(int hp) {
        this.maxPlayerHp = hp;
        this.playerHp = hp;
    }

    public void setMaxNpcHp(int hp) {
        this.maxNpcHp = hp;
        this.npcHp = hp;
    }
}