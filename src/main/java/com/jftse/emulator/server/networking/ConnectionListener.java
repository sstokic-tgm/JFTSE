package com.jftse.emulator.server.networking;

import com.jftse.emulator.server.game.core.handler.AbstractHandler;
import com.jftse.emulator.server.game.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public interface ConnectionListener {

    void connected(Connection connection);
    void disconnected(Connection connection);

    default void received(Connection connection, Packet packet) {
        Class<? extends AbstractHandler> handlerClass = PacketOperations.handlerOf(packet.getPacketId());

        try {
            AbstractHandler handler = handlerClass.getDeclaredConstructor().newInstance();
            handler.setConnection(connection);
            if (handler.process(packet))
                handler.handle();

        } catch (Exception e) {
            onException(connection, e);
        }
    }

    void idle(Connection connection);
    void onException(Connection connection, Exception exception);
    void onTimeout(Connection connection);
    void cleanUp();
}
