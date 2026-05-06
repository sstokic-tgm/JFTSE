package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.CMSGLobbyOption;
import com.jftse.server.core.shared.packets.lobby.SMSGLobbyOption;

@PacketId(CMSGLobbyOption.PACKET_ID)
public class LobbyOptionPacketHandler implements PacketHandler<FTConnection, CMSGLobbyOption> {
    /**
     * option 10: create room
     * option 12: join room
     * option 17: create quick/join room (chat room)
     * option 18: create quick/join room (my home)
     */

    @Override
    public void handle(FTConnection connection, CMSGLobbyOption packet) {
        FTClient client = connection.getClient();
        if (client == null) {
            return;
        }

        byte option = packet.getOption();
        if (option == 10 || option == 12 || option == 17 || option == 18) {
            if (client.isInLobby()) {
                client.setInLobby(false);
            }
            GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        }

        SMSGLobbyOption lobbyOptionPacket = SMSGLobbyOption.builder().option(option).build();
        connection.sendTCP(lobbyOptionPacket);
    }
}
