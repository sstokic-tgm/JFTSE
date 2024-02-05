package com.jftse.entities.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemEnchantLevel extends Item {
    private String elementalKind;
    private Double basicPercentage;
    private Integer failedPercentage;
    private Integer grade;
    private Integer downgrade;
    private Integer minEfficiency;
    private Integer maxEfficiency;
    private Integer requireGold;
}
