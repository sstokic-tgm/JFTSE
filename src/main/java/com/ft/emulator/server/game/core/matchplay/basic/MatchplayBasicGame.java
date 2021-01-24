package com.ft.emulator.server.game.core.matchplay.basic;

import com.ft.emulator.server.game.core.constants.ServeType;
import com.ft.emulator.server.game.core.matchplay.MatchplayGame;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import lombok.Getter;
import lombok.Setter;

import java.awt.*;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

@Getter
@Setter
public class MatchplayBasicGame extends MatchplayGame {
    private byte pointsRedTeam;
    private byte pointsBlueTeam;
    private byte setsRedTeam;
    private byte setsBlueTeam;
    private RoomPlayer servePlayer;
    private RoomPlayer receiverPlayer;

    public MatchplayBasicGame() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setStartTime(cal.getTime());

        this.pointsRedTeam = 0;
        this.pointsBlueTeam = 0;
        this.setsRedTeam = 0;
        this.setsBlueTeam = 0;
        this.setFinished(false);
    }

    public void setPoints(byte pointsRedTeam, byte pointsBlueTeam) {
        this.pointsRedTeam = pointsRedTeam;
        this.pointsBlueTeam = pointsBlueTeam;

        if (pointsRedTeam == 4 && pointsBlueTeam < 3) {
            this.setsRedTeam++;
            resetPoints();
        }
        else if (pointsRedTeam > 4 && (pointsRedTeam - pointsBlueTeam) == 2) {
            this.setsRedTeam++;
            resetPoints();
        }
        else if (pointsBlueTeam == 4 && pointsRedTeam < 3) {
            this.setsBlueTeam++;
            resetPoints();
        }
        else if (pointsBlueTeam > 4 && (pointsBlueTeam - pointsRedTeam) == 2) {
            this.setsBlueTeam++;
            resetPoints();
        }

        if (this.setsRedTeam == 2 || this.setsBlueTeam == 2) {
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

    @Override
    public boolean isRedTeam(int playerPos) {
        return playerPos == 0 || playerPos == 2;
    }

    @Override
    public boolean isBlueTeam(int playerPos) {
        return playerPos == 1 || playerPos == 3;
    }

    public byte getServeType(boolean willServeBall, boolean isInServingTeam, Point playerLocation) {
        if (willServeBall) {
            return ServeType.ServeBall;
        }

        if (!isInServingTeam && Math.abs(playerLocation.y) == 125) {
            return ServeType.ReceiveBall;
        }

        return ServeType.None;
    }

    public Point getStartingLocation(boolean isSingles, boolean isInServingTeam, boolean willServeBall, List<Point> playerLocationsOnMap, int playerPosition) {
        Point playerLocation = playerLocationsOnMap.get(playerPosition);
        if (isSingles) {
            return playerLocation;
        }

        long servingPosY = 125;
        long nonServingPosY = 75;
        long posY = playerLocation.y;
        if (!isInServingTeam) {
            if (playerLocation.y == servingPosY) {
                posY = nonServingPosY;
            }
            if (playerLocation.y == -servingPosY) {
                posY = -nonServingPosY;
            }
            if (playerLocation.y == nonServingPosY) {
                posY = servingPosY;
            }
            if (playerLocation.y == -nonServingPosY) {
                posY = -servingPosY;
            }

            playerLocation.setLocation(playerLocation.x, posY);
            return playerLocation;
        }

        if (willServeBall) {
            posY = playerLocation.y > 0 ? servingPosY : -servingPosY;
        } else {
            posY = playerLocation.y > 0 ? nonServingPosY : -nonServingPosY;
        }

        playerLocation.setLocation(playerLocation.x, posY);
        return playerLocation;
    }

    public Point invertPointX(Point point) {
        return new Point(point.x * (-1), point.y);
    }

    public Point invertPointY(Point point) {
        return new Point(point.x, point.y  * (-1));
    }

    public boolean shouldSwitchServingSide(boolean isSingles, boolean isRedTeamServing, boolean anyTeamWonSet, int playerPosition) {
        if (anyTeamWonSet) {
            return false;
        }

        if (isSingles) {
            return true;
        }
        return isRedTeamServing && this.isRedTeam(playerPosition) || !isRedTeamServing && this.isBlueTeam(playerPosition);
    }

    public boolean isRedTeamServing(int timesCourtChanged) {
        return this.isEven(timesCourtChanged);
    }

    public boolean shouldPlayerServe(boolean isSingles, int timesCourtChanged, int playerPosition) {
        if (isSingles) {
            if (this.isEven(playerPosition) && this.isEven(timesCourtChanged)) {
                return true;
            }

            return !this.isEven(playerPosition) && !this.isEven(timesCourtChanged);
        } else {
            return playerPosition == timesCourtChanged;
        }
    }

    private boolean isEven(int number) {
        return number % 2 == 0;
    }
}