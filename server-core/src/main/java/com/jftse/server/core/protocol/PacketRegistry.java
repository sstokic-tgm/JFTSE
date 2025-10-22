package com.jftse.server.core.protocol;

import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.net.Client;
import com.jftse.server.core.net.Connection;
import com.jftse.server.core.shared.packets.CMSGDefault;
import lombok.extern.log4j.Log4j2;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
public final class PacketRegistry {
    private static final Map<Integer, PacketFactory> REGISTRY = new HashMap<>();
    private static final Map<Integer, PacketHandler<? extends Connection<?>, ? extends IPacket>> HANDLERS = new HashMap<>();

    private PacketRegistry() {
    }

    public static void register(int packetId, PacketFactory factory) {
        String className = factory.getClass().getName();
        className = className.substring(0, className.indexOf("$"));
        log.info("{} {}", className, String.format("0x%X(%d)", packetId, packetId));
        REGISTRY.put(packetId, factory);
    }

    public static void registerHandlers() {
        log.info("Registering handlers...");

        Reflections reflections = new Reflections(new ConfigurationBuilder().forPackages("com.jftse.emulator.server.core.handler"));

        final Set<Class<? extends PacketHandler>> packetHandlers = reflections.getSubTypesOf(PacketHandler.class);
        final Map<Integer, Class<? extends PacketHandler>> packetHandlersMap = packetHandlers.stream()
                .filter(handler -> handler.isAnnotationPresent(PacketId.class))
                .collect(
                        Collectors.toMap(
                                handler -> handler.getAnnotation(PacketId.class).value(),
                                handler -> handler));
        packetHandlersMap.forEach((key, value) -> {
            log.info(value.getCanonicalName() + " " + String.format("0x%X(%d)", key, key));
            try {
                PacketHandler<? extends Connection<?>, ? extends IPacket> handlerInstance = (PacketHandler<? extends Connection<?>, ? extends IPacket>) value.getDeclaredConstructor().newInstance();
                HANDLERS.put(key, handlerInstance);
            } catch (Exception e) {
                log.error("Failed to instantiate handler for packet ID: 0x{} ({})", Integer.toHexString(key), key, e);
            }
        } );
    }

    public static IPacket decode(int packetId, byte[] data) {
        PacketFactory factory = REGISTRY.get(packetId);
        if (factory == null) {
            factory = REGISTRY.get(0);
        }
        return factory != null ? factory.create(data) : CMSGDefault.fromBytes(data);
    }

    @SuppressWarnings("unchecked")
    public static <C extends Connection<? extends Client<C>>, T extends IPacket> PacketHandler<C, T> getHandler(int packetId) {
        return (PacketHandler<C, T>) HANDLERS.get(packetId);
    }
}
