package com.jftse.emulator.server.core.life.lottery;

import com.jftse.entities.database.model.item.Product;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LotteryResolvedEntry {
    private int gachaIndex;
    private int productIndex;
    private Product product;
    private int rarityLevel;
    private Integer qtyMin;
    private Integer qtyMax;
    private Double weight;
    private Integer gachaTokens;
}
