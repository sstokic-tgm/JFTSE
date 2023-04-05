package com.jftse.emulator.server.core.matchplay;

import com.jftse.emulator.server.core.packets.matchplay.C2SMatchplayPointPacket;
import com.jftse.emulator.server.net.FTClient;

public interface MatchplayHandleable {
    void onStart(final FTClient ftClient);
    void onEnd(final FTClient ftClient);
    void onPrepare(final FTClient ftClient);
    void onPoint(final FTClient ftClient, C2SMatchplayPointPacket matchplayPointPacket);
}
