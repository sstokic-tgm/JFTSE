package com.jftse.emulator.server.database.model.level;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class LevelExp extends AbstractIdBaseModel {
    @Column(unique = true)
    private Byte level;
    private Integer expValue;
}
