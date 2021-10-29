package com.jftse.emulator.common.model.config;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
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
