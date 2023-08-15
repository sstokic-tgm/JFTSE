package com.jftse.emulator.server.core.command;

import com.jftse.emulator.server.net.FTConnection;

import java.util.List;

public interface Command {
    void execute(FTConnection connection, List<String> params);
}
