package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "K_Status")
public class KStatus extends AbstractIdBaseModel {
    private String name;

    public static Long ACTIVE = 1L;
    public static Long INACTIVE = 2L;
    public static Long OPEN = 3L;
    public static Long CANCELLED = 4L;
}
