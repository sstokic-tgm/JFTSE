package com.jftse.entities.database.model.player;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class EquippedItemStats {
    private Integer strength = 0;
    private Integer stamina = 0;
    private Integer dexterity = 0;
    private Integer willpower = 0;

    private Integer addHp = 0;

    private Integer enchantStr = 0;
    private Integer enchantSta = 0;
    private Integer enchantDex = 0;
    private Integer enchantWil = 0;
}
