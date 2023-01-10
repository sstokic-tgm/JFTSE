package com.jftse.entities.database.model.log;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Lob;

@Getter
@Setter
@Entity
public class GameLog extends AbstractBaseModel {
    private GameLogType gameLogType;
    @Lob
    private String content;
}
