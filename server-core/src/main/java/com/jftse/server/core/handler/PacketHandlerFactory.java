package com.jftse.server.core.handler;

import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class PacketHandlerFactory {
    private static PacketHandlerFactory instance;

    private final HashMap<PacketOperations, Class<? extends AbstractPacketHandler>> handlerMap = new HashMap<>(PacketOperations.values().length);

    public PacketHandlerFactory() {
        Arrays.stream(PacketOperations.values()).forEach(packetOperation -> handlerMap.put(packetOperation, null));
    }

    public static PacketHandlerFactory initFactory() {
        if (instance == null) {
            instance = new PacketHandlerFactory();
            return instance;
        }
        return instance;
    }

    public void autoRegister() {
        Reflections reflections = new Reflections("com.jftse.emulator.server.core.handler");

        Set<Class<? extends AbstractPacketHandler>> packetHandlers = reflections.getSubTypesOf(AbstractPacketHandler.class);
        final Map<PacketOperations, Class<? extends AbstractPacketHandler>> packetHandlersMap = packetHandlers.stream()
                .filter(abstractPacketHandler -> abstractPacketHandler.isAnnotationPresent(PacketOperationIdentifier.class))
                .collect(
                        Collectors.toMap(
                                handler -> handler.getAnnotation(PacketOperationIdentifier.class).value(),
                                handler -> handler));
        packetHandlersMap.forEach((key, value) -> System.out.println(key.getValue() + " " + value.getCanonicalName()));
        registerHandlerMap(packetHandlersMap);
    }

    public void registerHandler(final PacketOperations packetOperation, Class<? extends AbstractPacketHandler> handler) {
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
