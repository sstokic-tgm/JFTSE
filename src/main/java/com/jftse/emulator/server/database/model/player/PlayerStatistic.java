package com.jftse.emulator.server.database.model.player;

import com.jftse.emulator.common.model.AbstractBaseModel;
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
    private Integer consecutiveWins = 0;
    private Integer maxConsecutiveWins = 0;
    private Integer numberOfDisconnects = 0;

    @NotAudited
    @Formula("basicRecordWin + basicRecordLoss + battleRecordWin + battleRecordLoss")
    private Integer totalGames;
}