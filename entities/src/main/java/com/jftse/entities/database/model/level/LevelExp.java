package com.jftse.entities.database.model.level;

import com.jftse.entities.database.model.AbstractIdBaseModel;
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
