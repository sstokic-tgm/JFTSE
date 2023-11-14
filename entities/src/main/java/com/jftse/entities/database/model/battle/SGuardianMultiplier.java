package com.jftse.entities.database.model.battle;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "S_Guardian_Multiplier")
public class SGuardianMultiplier extends AbstractIdBaseModel {
    private String description;
    private Double multiplier;
}
