package com.ft.emulator.server.database.model.gameserver;

import com.ft.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class GameServerType extends AbstractBaseModel {

    @Column(unique = true)
    private Byte type;

    @Column(unique = true)
    private String name;
}