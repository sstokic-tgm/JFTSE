package com.jftse.server.core.rabbit;

public abstract class AbstractMessageHandler<T extends AbstractBaseMessage> {
    public abstract void register(MessageHandlerRegistry registry);
    public abstract void handle(T message);
}
