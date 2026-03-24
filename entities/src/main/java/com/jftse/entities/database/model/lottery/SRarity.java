package com.jftse.entities.database.model.lottery;

import com.jftse.entities.database.model.AbstractBaseModel;
import com.jftse.entities.database.model.KStatus;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "S_Rarity",
        indexes = {
                @Index(name = "idx_srarity_raritylevel_statusid", columnList = "rarity_level, status_id"),
                @Index(name = "idx_srarity_name", columnList = "name")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"name"}),
                @UniqueConstraint(columnNames = {"rarity_level"})
        })
public class SRarity extends AbstractBaseModel {
    private String name;
    private String description;
    @Column(name = "rarity_level")
    private int rarityLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;
}
