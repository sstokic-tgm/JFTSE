package com.jftse.entities.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemEnchant extends Item {
    private String useType;

    private Integer maxUse;

    private String kind;
    private String elementalKind;

    private Integer sellPrice;

    private Integer addPercentage;
    private Integer itemGrade;
    private Boolean hair;
    private Boolean body;
    private Boolean pants;
    private Boolean foot;
    private Boolean cap;
    private Boolean hand;
    private Boolean glasses;
    private Boolean bag;
    private Boolean socks;
    private Boolean racket;
}
