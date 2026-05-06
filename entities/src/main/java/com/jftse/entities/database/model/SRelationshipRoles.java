package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Table;

@Getter
@Setter
@Entity
@Table(name = "S_Relationship_Roles")
public class SRelationshipRoles extends AbstractIdBaseModel {
    private String name;
}
