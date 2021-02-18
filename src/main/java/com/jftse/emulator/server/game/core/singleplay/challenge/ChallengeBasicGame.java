package com.jftse.emulator.server.game.core.singleplay.challenge;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class ChallengeBasicGame extends ChallengeGame {
    private byte pointsPlayer;
    private byte pointsNpc;
    private byte setsPlayer;
    private byte setsNpc;

    public ChallengeBasicGame(int challengeIndex) {
        this.setChallengeIndex(challengeIndex);

        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());

        this.pointsPlayer = 0;
        this.pointsNpc = 0;
        this.setsPlayer = 0;
        this.setsNpc = 0;
        this.setFinished(false);
    }

    public void setPoints(byte pointsPlayer, byte pointsNpc) {
        this.pointsPlayer = pointsPlayer;
        this.pointsNpc = pointsNpc;

        if(pointsPlayer == 4 && pointsNpc < 3) {
            this.setsPlayer++;
            resetPoints();
        }
        else if(pointsPlayer > 4 && (pointsPlayer - pointsNpc) == 2) {
            this.setsPlayer++;
            resetPoints();
        }
        else if(pointsNpc == 4 && pointsPlayer < 3) {
            this.setsNpc++;
            resetPoints();
        }
        else if(pointsNpc > 4 && (pointsNpc - pointsPlayer) == 2) {
            this.setsNpc++;
            resetPoints();
        }

        if(this.setsPlayer == 2 || this.setsNpc == 2) {
            this.setFinished(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setEndTime(cal.getTime());
        }
    }

    private void resetPoints() {
        this.pointsPlayer = 0;
        this.pointsNpc = 0;
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }
}
