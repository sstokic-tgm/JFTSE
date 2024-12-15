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
     * option 16: create quick/join room (guardian)
     * option 14: create quick/join room (battle)
     * option 13: create quick/join room (basic)
     * option 15: create quick/join room (battlemon)
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
        if (option == 10 || option == 12 || option == 16 || option == 14 || option == 13 || option == 15) {
            if (client.isInLobby()) {
                client.setInLobby(false);
            }
            GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        }

        S2CLobbyOptionPacket lobbyOptionPacket = new S2CLobbyOptionPacket(option);
        connection.sendTCP(lobbyOptionPacket);
    }
}
