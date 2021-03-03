package com.jftse.emulator.server.database.model.item;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Entity;

@Getter
@Setter
@Entity
public class ItemRecipe extends Item {
    private String useType;

    private Integer maxUse;

    private Integer useCount;

    private String kind;

    private String forPlayer;

    private Boolean enableParcel;
}
