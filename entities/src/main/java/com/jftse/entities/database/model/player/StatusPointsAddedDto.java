package com.jftse.entities.database.model.player;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatusPointsAddedDto {
    private Byte strength = 0;
    private Byte stamina = 0;
    private Byte dexterity = 0;
    private Byte willpower = 0;

    private Integer addHp = 0;

    private Integer addStr = 0;
    private Integer addSta = 0;
    private Integer addDex = 0;
    private Integer addWil = 0;
}
