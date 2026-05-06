package com.jftse.entities.database.model.battle;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.KStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "Skill_2_Guardians")
public class Skill2Guardians extends AbstractBaseModel {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guardian_2_maps_id", referencedColumnName = "id")
    private Guardian2Maps guardian2Maps;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;

    private Integer btItemID;
    private Double chance;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skill_id", referencedColumnName = "id", nullable = false)
    private Skill skill;
}
