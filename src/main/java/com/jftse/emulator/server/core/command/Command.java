package com.jftse.emulator.server.core.command;

import com.jftse.emulator.server.networking.Connection;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public abstract class Command {
    protected int rank;
    protected String description;

    public abstract void execute(Connection connection, List<String> params);
}
