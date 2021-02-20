package com.jftse.emulator.server.database.model.gameserver;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
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
}
