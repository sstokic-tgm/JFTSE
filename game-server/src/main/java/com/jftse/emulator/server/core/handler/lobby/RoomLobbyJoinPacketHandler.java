package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.lobby.CMSGRoomLobbyJoin;
import com.jftse.server.core.shared.packets.lobby.SMSGRoomLobbyJoin;

@PacketId(CMSGRoomLobbyJoin.PACKET_ID)
public class RoomLobbyJoinPacketHandler implements PacketHandler<FTConnection, CMSGRoomLobbyJoin> {
    @Override
    public void handle(FTConnection connection, CMSGRoomLobbyJoin packet) {
        FTClient client = connection.getClient();
        if (client == null) {
            return;
        }

        SMSGRoomLobbyJoin response = SMSGRoomLobbyJoin.builder().result((short) 0).build();
        connection.sendTCP(response);
    }
}
