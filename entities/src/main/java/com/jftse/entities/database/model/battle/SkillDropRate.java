package com.jftse.entities.database.model.battle;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class SkillDropRate extends AbstractIdBaseModel {
    private Integer fromLevel;
    private Integer toLevel;
    private String dropRates;
}
