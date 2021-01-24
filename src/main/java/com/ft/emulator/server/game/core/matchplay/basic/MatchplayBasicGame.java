package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.TimeZone;

@Getter
@Setter
public class MatchplayBasicGame extends MatchplayGame {
    private byte pointsRedTeam;
    private byte pointsBlueTeam;
    private byte setsRedTeam;
    private byte setsBlueTeam;

    public MatchplayBasicGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());

        this.pointsRedTeam = 0;
        this.pointsBlueTeam = 0;
        this.setsRedTeam = 0;
        this.setsBlueTeam = 0;
        this.setFinished(false);
    }

    public void setPoints(byte pointsPlayer1, byte pointsPlayer2) {
        this.pointsRedTeam = pointsPlayer1;
        this.pointsBlueTeam = pointsPlayer2;

        if(pointsPlayer1 == 4 && pointsPlayer2 < 3) {
            this.setsRedTeam++;
            resetPoints();
        }
        else if(pointsPlayer1 > 4 && (pointsPlayer1 - pointsPlayer2) == 2) {
            this.setsRedTeam++;
            resetPoints();
        }
        else if(pointsPlayer2 == 4 && pointsPlayer1 < 3) {
            this.setsBlueTeam++;
            resetPoints();
        }
        else if(pointsPlayer2 > 4 && (pointsPlayer2 - pointsPlayer1) == 2) {
            this.setsBlueTeam++;
            resetPoints();
        }

        if(this.setsRedTeam == 2 || this.setsBlueTeam == 2) {
            this.setFinished(true);

            Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            this.setEndTime(cal.getTime());
        }
    }

    private void resetPoints() {
        this.pointsRedTeam = 0;
        this.pointsBlueTeam = 0;
    }

    @Override
    public long getTimeNeeded() {
        return getEndTime().getTime() - getStartTime().getTime();
    }
}