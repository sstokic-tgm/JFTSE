package com.jftse.entities.database.model.gameserver;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;

@Getter
@Setter
@Entity
public class GameServer extends AbstractIdBaseModel {
    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.DETACH)
    private GameServerType gameServerType;

    private String host;
    // char
    private Integer port;
    private Boolean isCustomChannel = false;
    private String name;
}
