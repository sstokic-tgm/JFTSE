package com.jftse.emulator.server.database.model.item;

import com.jftse.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemChar extends AbstractBaseModel {
    private Byte playerType;
    private Byte strength;
    private Byte stamina;
    private Byte dexterity;
    private Byte willpower;
}