package com.jftse.emulator.server.core.handler.lobby;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRequestRoomLobbyJoin)
public class RoomLobbyJoinPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null) {
            return;
        }

        Packet requestRoomJoinPacket = new Packet(PacketOperations.S2CRequestRoomLobbyJoin);
        requestRoomJoinPacket.write((short) 0);
        connection.sendTCP(requestRoomJoinPacket);
    }
}
