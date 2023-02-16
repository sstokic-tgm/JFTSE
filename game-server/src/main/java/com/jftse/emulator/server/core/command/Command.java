package com.jftse.emulator.server.core.command;

import com.jftse.emulator.server.net.FTConnection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class Command {
    protected int rank;
    protected String description;

    public abstract void execute(FTConnection connection, List<String> params);
}
