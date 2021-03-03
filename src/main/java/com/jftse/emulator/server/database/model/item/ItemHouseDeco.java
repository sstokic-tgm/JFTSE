package com.jftse.emulator.server.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemHouseDeco extends Item {
    private String kind;

    private String useType;

    private Integer maxUse;

    private Boolean enableParcel;

    private Integer housingPoint;
    private Byte addGold;
    private Byte addExp;
    private Byte addBattleGold;
    private Byte addBattleExp;
}
