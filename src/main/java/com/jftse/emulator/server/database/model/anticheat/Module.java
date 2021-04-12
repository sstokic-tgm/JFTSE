package com.jftse.emulator.server.database.model.anticheat;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Module extends AbstractBaseModel {
    private String name;
    private Boolean block;
}