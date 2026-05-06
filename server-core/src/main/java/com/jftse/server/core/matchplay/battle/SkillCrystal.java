package com.jftse.server.core.matchplay.battle;

import com.jftse.server.core.util.Time;
import lombok.Getter;
import lombok.Setter;

@Getter
public class SkillCrystal {
    private final int id;
    private final long timestamp;

    @Setter private long pickedUpByPlayerId = -1;
    @Setter private int skillIndex = -1;

    public SkillCrystal(int id) {
        this.id = id;
        this.timestamp = Time.getNSTime();
    }
}
