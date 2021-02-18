package com.jftse.emulator.server.database.model.lottery;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LotteryItemDto {

    private Integer shopIndex;
    private Integer quantityMin;
    private Integer quantityMax;
    private Double chansPer;
}
