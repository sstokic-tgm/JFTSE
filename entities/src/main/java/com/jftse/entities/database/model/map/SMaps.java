package com.jftse.entities.database.model.map;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "S_Maps")
public class SMaps extends AbstractBaseModel {
    private String name;
    private String description;
    @Column(nullable = false, columnDefinition = "TINYINT(20) DEFAULT 0")
    private Integer map = 0;
    private Boolean useBreathTime;
    private Integer breathTime;
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean isBossStage = false;
    private Integer bossPlayTime;
    private Integer triggerBossTime;
    private Integer playTime;
}
