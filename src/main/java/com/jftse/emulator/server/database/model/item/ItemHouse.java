package com.jftse.emulator.server.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemHouse extends Item {
    private String useType;

    private Byte level;

    private Integer housingPoint;
    private Byte maxAddPercent;
}
