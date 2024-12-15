package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SLobbyJoin)
public class LobbyJoinPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();

        if (!client.getIsJoiningOrLeavingLobby().compareAndSet(false, true)) {
            return;
        }

        if (!client.isInLobby()) {
            client.setInLobby(true);
        } else {
            return;
        }

        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        GameManager.getInstance().refreshLobbyPlayerListForAllClients();

        client.getIsJoiningOrLeavingLobby().set(false);
    }
}
