package com.jftse.entities.database.model.item;

import com.jftse.entities.database.model.AbstractBaseModel;
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