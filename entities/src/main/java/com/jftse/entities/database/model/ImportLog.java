package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ImportLog extends AbstractBaseModel {
    private String fileName;
    private Long importDate;
}
