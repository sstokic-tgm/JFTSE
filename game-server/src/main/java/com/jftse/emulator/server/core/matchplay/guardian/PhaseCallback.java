package com.jftse.emulator.server.core.matchplay.guardian;

import com.jftse.emulator.server.net.FTConnection;

public interface PhaseCallback {
    void onNextPhase(FTConnection connection);
    void onPhaseEnd(FTConnection connection);
    //void onPhaseStart();
    //void onPhaseUpdate();
}
