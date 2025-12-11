package com.jftse.emulator.server.core.matchplay;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.shared.packets.matchplay.CMSGPoint;

public interface MatchplayHandleable {
    void onStart(final FTClient ftClient);
    void onEnd(final FTClient ftClient);
    void onPrepare(final FTClient ftClient);
    void onPoint(final FTClient ftClient, CMSGPoint pointPacket);
}
