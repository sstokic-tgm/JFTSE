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
     * option 16: create quick/join room (guardian)
     * option 14: create quick/join room (battle)
     * option 13: create quick/join room (basic)
     * option 15: create quick/join room (battlemon)
     */

    @Override
    public void handle(FTConnection connection, CMSGLobbyOption packet) {
        FTClient client = connection.getClient();
        if (client == null) {
            return;
        }

        byte option = packet.getOption();
        if (option == 10 || option == 12 || option == 16 || option == 14 || option == 13 || option == 15) {
            if (client.isInLobby()) {
                client.setInLobby(false);
            }
            GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        }

        SMSGLobbyOption lobbyOptionPacket = SMSGLobbyOption.builder().option(option).build();
        connection.sendTCP(lobbyOptionPacket);
    }
}
