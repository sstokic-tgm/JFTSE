package com.ft.emulator.server.networking;

import com.ft.emulator.server.networking.packet.Packet;

public interface ConnectionListener {

    void connected(Connection connection);
    void disconnected(Connection connection);
    void received(Connection connection, Packet packet);
    void idle(Connection connection);
}