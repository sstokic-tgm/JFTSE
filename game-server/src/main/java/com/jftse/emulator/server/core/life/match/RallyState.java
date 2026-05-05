package com.jftse.emulator.server.core.life.match;

import com.jftse.server.core.constants.BallHitAction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RallyState {
    private int serverPosition = -1;
    private int lastHitterPosition = -1;
    private int rallyCount = 0;
    private BallHitAction lastHitAct;

    public void reset() {
        serverPosition = -1;
        lastHitterPosition = -1;
        rallyCount = 0;
        lastHitAct = null;
    }
}
