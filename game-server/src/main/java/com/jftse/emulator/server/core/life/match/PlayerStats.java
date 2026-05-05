package com.jftse.emulator.server.core.life.match;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlayerStats {
    private int stroke = 0;
    private int slice = 0;
    private int lob = 0;
    private int smash = 0;
    private int volley = 0;
    private int topSpin = 0;
    private int rising = 0;
    private int serve = 0;
    private int guardBreakShot = 0;
    private int chargeShot = 0;
    private int skillShot = 0;
}
