package com.jftse.emulator.server.game.core.matchplay.battle;

import lombok.Getter;
import lombok.Setter;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Getter
@Setter
public class SkillCrystal {
    private short id;
    private Date timeSpawned;

    public SkillCrystal() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        this.setTimeSpawned(cal.getTime());
    }
}
