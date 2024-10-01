package com.jftse.entities.database.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "S_Relationships")
public class SRelationships extends AbstractBaseModel {
    private Long id_f;
    private Long id_t;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private SRelationshipRoles role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relationship_id", referencedColumnName = "id", nullable = false)
    private SRelationshipTypes relationship;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id", nullable = false)
    private KStatus status;

    private Integer qtyMin;
    private Integer qtyMax;
    private Integer qty;
    private Double weight;
    private Integer levelReq;
    private Boolean forHardMode;
    private Boolean forRandomMode;
    private Boolean forDoubles;
}
