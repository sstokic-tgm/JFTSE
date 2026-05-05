package com.jftse.entities.database.model.player;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Formula;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class PlayerStatistic extends AbstractBaseModel {
    private Integer basicRecordWin = 0;
    private Integer basicRecordLoss = 0;
    private Integer battleRecordWin = 0;
    private Integer battleRecordLoss = 0;
    private Integer guardianRecordWin = 0;
    private Integer guardianRecordLoss = 0;
    private Integer basicRP = 0;
    private Integer battleRP = 0;
    private Integer guardianRP = 0;
    private Integer consecutiveWins = 0;
    private Integer maxConsecutiveWins = 0;

    private Integer numberOfDisconnects = 0;

    private Integer serviceAce = 0;
    private Integer returnAce = 0;
    private Integer stroke = 0;
    private Integer slice = 0;
    private Integer lob = 0;
    private Integer smash = 0;
    private Integer volley = 0;
    private Integer topSpin = 0;
    private Integer rising = 0;
    private Integer serve = 0;
    private Integer guardBreakShot = 0;
    private Integer chargeShot = 0;
    private Integer skillShot = 0;

    @NotAudited
    @Formula("basicRecordWin + basicRecordLoss + battleRecordWin + battleRecordLoss")
    private Integer totalGames;
}