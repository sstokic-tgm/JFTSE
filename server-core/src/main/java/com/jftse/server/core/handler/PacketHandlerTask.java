package com.jftse.server.core.handler;

@FunctionalInterface
public interface PacketHandlerTask<T> {
    void accept(T t) throws Exception;
}
