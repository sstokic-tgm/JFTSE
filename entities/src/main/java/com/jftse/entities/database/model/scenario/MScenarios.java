package com.jftse.entities.database.model.scenario;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.KStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "M_Scenarios")
public class MScenarios extends AbstractBaseModel {
    private String description;
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean isDefault = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "component_of_id")
    private MScenarios componentOf;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;

    @Enumerated(EnumType.STRING)
    private GameMode gameMode;

    public enum GameMode {
        BASIC, BATTLE, GUARDIAN, BOSS_BATTLE
    }
}
