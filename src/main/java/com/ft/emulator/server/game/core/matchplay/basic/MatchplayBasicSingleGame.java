package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class MatchplayBasicSingleGame extends MatchplayGame {
    // maybe a rename to blue/red instead of player, not looking good if for double for example there is 1, 2, 3, 4
    private byte pointsPlayer1;
    private byte pointsPlayer2;
    private byte setsPlayer1;
    private byte setsPlayer2;

    public MatchplayBasicSingleGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());

        this.pointsPlayer1 = 0;
        this.pointsPlayer2 = 0;
        this.setsPlayer1 = 0;
        this.setsPlayer2 = 0;
        this.setFinished(false);
    }

    public void setPoints(byte pointsPlayer1, byte pointsPlayer2) {
        this.pointsPlayer1 = pointsPlayer1;
        this.pointsPlayer2 = pointsPlayer2;

        if(pointsPlayer1 == 4 && pointsPlayer2 < 3) {
            this.setsPlayer1++;
            resetPoints();
        }
        else if(pointsPlayer1 > 4 && (pointsPlayer1 - pointsPlayer2) == 2) {
            this.setsPlayer1++;
            resetPoints();
        }
        else if(pointsPlayer2 == 4 && pointsPlayer1 < 3) {
            this.setsPlayer2++;
            resetPoints();
        }
        else if(pointsPlayer2 > 4 && (pointsPlayer2 - pointsPlayer1) == 2) {
            this.setsPlayer2++;
            resetPoints();
        }

        if(this.setsPlayer1 == 2 || this.setsPlayer2 == 2) {
            this.setFinished(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setEndTime(cal.getTime());
        }
    }

    private void resetPoints() {
        this.pointsPlayer1 = 0;
        this.pointsPlayer2 = 0;
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }
}