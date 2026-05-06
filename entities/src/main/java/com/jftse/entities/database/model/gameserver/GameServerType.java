package com.jftse.entities.database.model.gameserver;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class GameServerType extends AbstractIdBaseModel {
    @Column(unique = true)
    private Byte type;

    @Column(unique = true)
    private String name;
}
