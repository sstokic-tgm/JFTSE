package com.jftse.emulator.server.game.core.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class ChallengeBattleGame extends ChallengeGame {

    private int playerHp, maxPlayerHp;
    private int npcHp, maxNpcHp;

    public ChallengeBattleGame(int challengeIndex) {
        this.setChallengeIndex(challengeIndex);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());

        this.maxPlayerHp = 0;
        this.playerHp = 0;
        this.maxNpcHp = 0;
        this.npcHp = 0;

        this.setFinished(false);
    }

    public void setHp(byte player, int hp) {
        if(player == 1) {
            this.playerHp += hp;
            if(this.playerHp > this.maxPlayerHp)
                this.playerHp = this.maxPlayerHp;
        }
        else {
            this.npcHp += hp;
            if(this.npcHp > this.maxNpcHp)
                this.npcHp = this.maxNpcHp;
        }

        if(this.playerHp <= 0 || this.npcHp <= 0) {
            this.setFinished(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setEndTime(cal.getTime());
        }
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }

    public void setMaxPlayerHp(int hp) {
        this.maxPlayerHp = hp;
        this.setPlayerHp(hp);
    }

    public void setMaxNpcHp(int hp) {
        this.maxNpcHp = hp;
        this.setNpcHp(hp);
    }
}
