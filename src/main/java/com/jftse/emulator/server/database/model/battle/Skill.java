package com.jftse.emulator.server.database.model.battle;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Skill extends AbstractIdBaseModel {
    private String name;
    private Integer texID;
    private Integer iconID;
    private Integer elemental;
    private Integer shotType;
    private Integer shotCnt;
    private Double chantTime;
    private Double randMaxTime;
    private Double playTime;
    private String vibration;
    private Integer soundShotID;
    private Integer soundHitID;
    private Integer damage;
    private Integer damageRate;
    private String damageInfo;
    private Integer property;
    private Integer targeting;
    private String tPosition;
    private Double radius;
    private Double shotSpeed;
    private Double shotRot;
    private Integer explosion;
    private Double coolingTime;
    private Double gdCoolingTime;
    private Double addEftTime0;
    private Double addEftTime1;
    private Double addSspAttTime0;
    private Double addSspAttTime1;
}
