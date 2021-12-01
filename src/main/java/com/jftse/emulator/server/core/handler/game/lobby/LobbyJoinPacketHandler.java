package com.jftse.emulator.server.core.handler.game.lobby;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.networking.packet.Packet;

public class LobbyJoinPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        if (!connection.getClient().getInLobby().compareAndSet(false, true)) {
            connection.getClient().getInLobby().set(true);
        }
        connection.getClient().getLobbyCurrentRoomListPage().getAndSet(-1);

        GameManager.getInstance().handleRoomPlayerChanges(connection, true);
        GameManager.getInstance().refreshLobbyPlayerListForAllClients();
    }
}
