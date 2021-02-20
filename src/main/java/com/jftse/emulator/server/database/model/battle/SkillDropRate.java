package com.jftse.emulator.server.database.model.battle;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONArray;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class SkillDropRate extends AbstractIdBaseModel {
    private Integer fromLevel;
    private Integer toLevel;
    private String dropRates;
}
