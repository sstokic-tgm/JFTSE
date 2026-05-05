package com.jftse.emulator.server.core.life.match;

import com.jftse.server.core.constants.BallHitAction;

public record RallyResult(boolean serviceAce, boolean returnAce, BallHitAction winningHit, int rallyCount,
                          int serverPosition, int lastHitterPosition) {
    public static RallyResult empty() {
        return new RallyResult(false, false, null, 0, -1, -1);
    }
}
