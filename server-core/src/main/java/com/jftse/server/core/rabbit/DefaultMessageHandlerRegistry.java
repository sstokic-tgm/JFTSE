package com.jftse.server.core.rabbit;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@Log4j2
public class DefaultMessageHandlerRegistry implements MessageHandlerRegistry {
    private final Map<String, AbstractMessageHandler<? extends AbstractBaseMessage>> handlers = new HashMap<>();

    @PostConstruct
    public void init() {
        log.debug("DefaultMessageHandlerRegistry initialized");
    }

    @Override
    public void register(String messageType, AbstractMessageHandler<? extends AbstractBaseMessage> handler) {
        handlers.put(messageType, handler);
    }

    @SuppressWarnings("unchecked")
    public AbstractMessageHandler<AbstractBaseMessage> getHandler(String messageType) {
        return (AbstractMessageHandler<AbstractBaseMessage>) handlers.get(messageType);
    }

    public void clear() {
        handlers.clear();
    }

    public boolean isEmpty() {
        return handlers.isEmpty();
    }

    public int size() {
        return handlers.size();
    }

    public boolean containsHandler(String messageType) {
        return handlers.containsKey(messageType);
    }

    public boolean containsHandler(AbstractMessageHandler<? extends AbstractBaseMessage> handler) {
        return handlers.containsValue(handler);
    }

    public void removeHandler(String messageType) {
        handlers.remove(messageType);
    }

    public void removeHandler(AbstractMessageHandler<? extends AbstractBaseMessage> handler) {
        handlers.values().removeIf(h -> h.equals(handler));
    }
}
