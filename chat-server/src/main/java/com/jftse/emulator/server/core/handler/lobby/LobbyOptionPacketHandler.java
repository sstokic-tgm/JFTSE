package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.C2SRequestLobbyOptionPacket;
import com.jftse.emulator.server.core.packets.lobby.S2CLobbyOptionPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRequestLobbyOption)
public class LobbyOptionPacketHandler extends AbstractPacketHandler {
    private C2SRequestLobbyOptionPacket requestLobbyOptionPacket;

    /**
     * option 10: create room
     * option 12: join room
     * option 17: create quick/join room (chat room)
     * option 18: create quick/join room (my home)
     */

    @Override
    public boolean process(Packet packet) {
        requestLobbyOptionPacket = new C2SRequestLobbyOptionPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null) {
            return;
        }

        byte option = requestLobbyOptionPacket.getOption();
        if (option == 10 || option == 12 || option == 17 || option == 18) {
            if (client.isInLobby()) {
                client.setInLobby(false);
            }
            GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        }

        S2CLobbyOptionPacket lobbyOptionPacket = new S2CLobbyOptionPacket(option);
        connection.sendTCP(lobbyOptionPacket);
    }
}
