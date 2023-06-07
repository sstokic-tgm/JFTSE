package com.jftse.emulator.server.core.interaction;

import com.jftse.emulator.server.net.FTClient;

public interface GameEventScriptable {
    String getName();
    String getType();
    String getDesc();
    void onEvent(FTClient client);
}
