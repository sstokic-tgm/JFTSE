package com.jftse.server.core.net;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Client<T extends Connection<?>> {
    protected T connection;

    protected String ip;
    protected int port;
}
