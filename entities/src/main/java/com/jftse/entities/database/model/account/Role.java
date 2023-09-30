package com.jftse.entities.database.model.account;

import com.jftse.entities.database.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class Role extends AbstractIdBaseModel {
    private String role;
    private String description;
}
