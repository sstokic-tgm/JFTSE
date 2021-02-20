package com.jftse.emulator.server.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemMaterial extends Item {
    private String useType;

    private Integer maxUse;

    private String kind;

    private Integer sellPrice;

    private Boolean enableParcel;
}
