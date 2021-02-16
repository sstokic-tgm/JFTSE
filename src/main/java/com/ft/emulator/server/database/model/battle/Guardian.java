package com.ft.emulator.server.database.model.battle;

import com.ft.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Guardian extends AbstractIdBaseModel {
    private String name;
    private Integer hpBase;
    private Integer hpPer;
    private Integer level;
    private Integer baseStr;
    private Integer baseSta;
    private Integer baseDex;
    private Integer baseWill;
    private Integer addStr;
    private Integer addSta;
    private Integer addDex;
    private Integer addWill;
    private Integer rewardExp;
    private Integer rewardGold;
}
