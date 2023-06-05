package com.jftse.emulator.server.core.command;

import com.jftse.emulator.server.net.FTConnection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class AbstractCommand implements Command {
    private int rank;
    private String description;
    private String commandName;

    public abstract void execute(FTConnection connection, List<String> params);
}
