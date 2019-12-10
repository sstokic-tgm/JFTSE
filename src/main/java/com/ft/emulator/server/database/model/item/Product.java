package com.ft.emulator.server.database.model.item;

import com.ft.emulator.common.model.AbstractBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Product extends AbstractBaseModel {

    @Column(unique = true)
    private Long productIndex;

    private Integer display;
    private Boolean hitDisplay;

    private Boolean enabled;

    private String useType;

    private Integer use0;
    private Integer use1;
    private Integer use2;

    private String priceType;

    private Integer oldPrice0;
    private Integer oldPrice1;
    private Integer oldPrice2;
    private Integer price0;
    private Integer price1;
    private Integer price2;
    private Integer couplePrice;

    private String category;

    private String name;

    private Integer goldBack;

    private Boolean enableParcel;

    private Byte forCharacter;

    private Long item0;
    private Long item1;
    private Long item2;
    private Long item3;
    private Long item4;
    private Long item5;
    private Long item6;
    private Long item7;
    private Long item8;
    private Long item9;
}