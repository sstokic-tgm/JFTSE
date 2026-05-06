package com.jftse.entities.database.model.anticheat;

import com.jftse.entities.database.model.AbstractBaseModel;
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