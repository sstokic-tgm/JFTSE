package com.jftse.emulator.server.database.model;

import com.jftse.emulator.common.model.AbstractBaseModel;
import com.jftse.emulator.server.database.model.tutorial.GameLogType;
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
