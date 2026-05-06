package com.jftse.entities.database.model.event;

import com.jftse.entities.database.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "SavedVariables",
        indexes = {
                @Index(name = "idx_script_account_name", columnList = "scriptId, accountId, name")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"scriptId", "accountId", "name"})
        }
)
public class SavedVariables extends AbstractBaseModel {
    @Column(nullable = false)
    private String scriptId;
    private Long accountId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String data;
}
