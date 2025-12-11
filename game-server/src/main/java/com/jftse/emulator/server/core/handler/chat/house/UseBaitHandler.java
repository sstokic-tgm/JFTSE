package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGThrowBait;
import com.jftse.server.core.shared.packets.chat.house.SMSGThrowBait;

@PacketId(CMSGThrowBait.PACKET_ID)
public class UseBaitHandler implements PacketHandler<FTConnection, CMSGThrowBait> {
    @Override
    public void handle(FTConnection connection, CMSGThrowBait packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || roomPlayer == null)
            return;

        if (roomPlayer.getUsedRod().compareAndSet(false, true)) {
            SMSGThrowBait throwBaitPacket = SMSGThrowBait.builder()
                    .playerPosition(roomPlayer.getPosition())
                    .displayMessage((byte) 0)
                    .build();
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(throwBaitPacket, connection);
        }
    }
}
