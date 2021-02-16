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
    private Byte level;
    private Byte baseStr;
    private Byte baseSta;
    private Byte baseDex;
    private Byte baseWill;
    private Byte addStr;
    private Byte addSta;
    private Byte addDex;
    private Byte addWill;
    private Integer rewardExp;
    private Integer rewardGold;
}
