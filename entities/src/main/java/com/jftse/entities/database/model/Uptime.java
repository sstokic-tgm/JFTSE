package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "uptime")
public class Uptime extends AbstractIdBaseModel {
    private ServerType serverType;
    private Long startTime;
    private Long uptime;
    private Integer maxPlayers;
    private String revision;
}
