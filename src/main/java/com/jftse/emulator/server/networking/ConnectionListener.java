package com.jftse.emulator.server.networking;

import com.jftse.emulator.server.networking.packet.Packet;

public interface ConnectionListener {

    void connected(Connection connection);
    void disconnected(Connection connection);
    void received(Connection connection, Packet packet);
    void idle(Connection connection);
    void onException(Connection connection, Exception exception);
    void onTimeout(Connection connection);

    void cleanUp();
}
