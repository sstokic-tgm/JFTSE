package com.jftse.entities.database.model.config;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class Config extends AbstractIdBaseModel {
    @Column(unique = true)
    private String name;
    private String description;
    private String value;
    private String type;
}
