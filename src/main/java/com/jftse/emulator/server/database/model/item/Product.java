package com.jftse.emulator.server.database.model.item;

import com.jftse.emulator.common.model.AbstractIdBaseModel;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
public class Product extends AbstractIdBaseModel {
    @Column(unique = true)
    private Integer productIndex;

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

    private Byte forPlayer;

    private Integer item0;
    private Integer item1;
    private Integer item2;
    private Integer item3;
    private Integer item4;
    private Integer item5;
    private Integer item6;
    private Integer item7;
    private Integer item8;
    private Integer item9;
}
