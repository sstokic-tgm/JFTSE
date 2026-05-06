package com.jftse.entities.database.model.event;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "S_GameEvent")
public class SGameEvent {
    @Id
    private Long id;

    private String name;
    private String type;
    private String description;
    private Date startDate;
    private Date endDate;
    private Boolean enabled;
}
