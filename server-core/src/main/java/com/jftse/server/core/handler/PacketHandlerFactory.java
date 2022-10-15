package com.jftse.server.core.handler;

import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class PacketHandlerFactory {
    private static PacketHandlerFactory instance;

    private final Logger log;
    private final HashMap<PacketOperations, Class<? extends AbstractPacketHandler>> handlerMap = new HashMap<>(PacketOperations.values().length);

    protected PacketHandlerFactory(Logger log) {
        this.log = log;
        Arrays.stream(PacketOperations.values()).forEach(packetOperation -> handlerMap.put(packetOperation, null));
    }

    public static PacketHandlerFactory initFactory(Logger log) {
        if (instance == null) {
            instance = new PacketHandlerFactory(log);
            return instance;
        }
        return instance;
    }

    public void autoRegister() {
        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("com.jftse.emulator.server.core.handler"));

        Set<Class<? extends AbstractPacketHandler>> packetHandlers = reflections.getSubTypesOf(AbstractPacketHandler.class);
        final Map<PacketOperations, Class<? extends AbstractPacketHandler>> packetHandlersMap = packetHandlers.stream()
                .filter(abstractPacketHandler -> abstractPacketHandler.isAnnotationPresent(PacketOperationIdentifier.class))
                .collect(
                        Collectors.toMap(
                                handler -> handler.getAnnotation(PacketOperationIdentifier.class).value(),
                                handler -> handler));
        log.info("Registering handlers...");
        packetHandlersMap.forEach((key, value) -> log.info(value.getCanonicalName() + " " + String.format("0x%X(%d)", key.getValue(), key.getValue())));
        registerHandlerMap(packetHandlersMap);
    }

    public void registerHandler(final PacketOperations packetOperation, Class<? extends AbstractPacketHandler> handler) {
        log.info("registered " + handler.getCanonicalName() + " " + String.format("0x%X(%d)", packetOperation.getValue(), packetOperation.getValue()));
        handlerMap.put(packetOperation, handler);
    }

    public void registerHandlerMap(final Map<PacketOperations, Class<? extends AbstractPacketHandler>> handlerMap) {
        this.handlerMap.putAll(handlerMap);
    }

    public AbstractPacketHandler getHandler(final PacketOperations packetOperation) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Class<? extends AbstractPacketHandler> handlerClass = handlerMap.get(packetOperation);
        return handlerClass == null ? null : handlerClass.getDeclaredConstructor().newInstance();
    }
}
