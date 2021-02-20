package com.jftse.emulator.server.database.model.player;

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
}
