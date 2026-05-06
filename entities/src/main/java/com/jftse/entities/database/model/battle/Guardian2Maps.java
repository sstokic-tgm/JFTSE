package com.jftse.entities.database.model.battle;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.KStatus;
import com.jftse.entities.database.model.map.SMaps;
import com.jftse.entities.database.model.scenario.MScenarios;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Guardian_2_Maps")
public class Guardian2Maps extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scenario_id", referencedColumnName = "id", nullable = false)
    private MScenarios scenario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "map_id", referencedColumnName = "id", nullable = false)
    private SMaps map;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_id", referencedColumnName = "id")
    private Guardian guardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "boss_guardian_id", referencedColumnName = "id")
    private BossGuardian bossGuardian;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('LEFT', 'RIGHT', 'MIDDLE')")
    private Side side;

    public enum Side {
        LEFT,
        RIGHT,
        MIDDLE
    }
}
