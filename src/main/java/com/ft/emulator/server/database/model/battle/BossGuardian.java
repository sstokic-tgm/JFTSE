package com.ft.emulator.server.database.model.battle;

import com.ft.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class BossGuardian extends AbstractIdBaseModel {
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

    public Guardian transformToGuardian() {
        Guardian guardian = new Guardian();
        guardian.setName(this.name);
        guardian.setHpBase(this.hpBase);
        guardian.setHpPer(this.hpPer);
        guardian.setLevel(this.level);
        guardian.setBaseStr(this.baseStr);
        guardian.setBaseSta(this.baseSta);
        guardian.setBaseDex(this.baseDex);
        guardian.setBaseWill(this.baseWill);
        guardian.setAddStr(this.addStr);
        guardian.setAddSta(this.addSta);
        guardian.setAddDex(this.addDex);
        guardian.setAddWill(this.addWill);
        guardian.setRewardExp(this.rewardExp);
        guardian.setRewardGold(this.rewardGold);
        return guardian;
    }
}