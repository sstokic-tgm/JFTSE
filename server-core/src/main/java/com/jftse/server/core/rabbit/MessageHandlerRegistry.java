package com.jftse.server.core.rabbit;

@FunctionalInterface
public interface MessageHandlerRegistry {
    void register(String messageType, AbstractMessageHandler<? extends AbstractBaseMessage> handler);
}
