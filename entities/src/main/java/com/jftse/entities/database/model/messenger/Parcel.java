package com.jftse.entities.database.model.messenger;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import javax.persistence.Column;
import javax.persistence.Entity;

@Getter
@Setter
@Audited
@Entity
public class Parcel extends AbstractMessage {
    private Integer gold;
    private EParcelType EParcelType;

    private String category;
    private Integer itemIndex;
    private Integer itemCount;
    private String useType;

    @Column(columnDefinition = "int default 0")
    private Integer enchantStr = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantSta = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantDex = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantWil = 0;

    @Column(columnDefinition = "int default 0")
    private Integer enchantElement = 0;
    @Column(columnDefinition = "int default 0")
    private Integer enchantLevel = 0;
}