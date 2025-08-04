package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CThrowRodPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.CMSG_ThrowBait)
public class UseBaitHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || roomPlayer == null)
            return;

        if (roomPlayer.getUsedRod().compareAndSet(false, true)) {
            S2CThrowRodPacket throwRodPacket = new S2CThrowRodPacket(roomPlayer.getPosition(), (byte) 0);
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(throwRodPacket, (FTConnection) connection);
        }
    }
}
