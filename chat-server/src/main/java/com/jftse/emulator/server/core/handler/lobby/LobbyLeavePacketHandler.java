package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.CMSGLobbyLeave;

@PacketId(CMSGLobbyLeave.PACKET_ID)
public class LobbyLeavePacketHandler implements PacketHandler<FTConnection, CMSGLobbyLeave> {
    @Override
    public void handle(FTConnection connection, CMSGLobbyLeave packet) {
        FTClient client = connection.getClient();

        if (!client.getIsJoiningOrLeavingLobby().compareAndSet(false, true)) {
            return;
        }

        if (client.isInLobby()) {
            client.setInLobby(false);
        }
        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().refreshLobbyPlayerListForAllClients();

        client.getIsJoiningOrLeavingLobby().set(false);
    }
}
