package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGSetBaitLocation;
import com.jftse.server.core.shared.packets.chat.house.SMSGSetBaitLocation;

@PacketId(CMSGSetBaitLocation.PACKET_ID)
public class SetBaitLocationHandler implements PacketHandler<FTConnection, CMSGSetBaitLocation> {
    @Override
    public void handle(FTConnection connection, CMSGSetBaitLocation packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        Room room = client.getActiveRoom();
        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (room == null || roomPlayer == null || !roomPlayer.getUsedRod().get())
            return;

        roomPlayer.setBaitX(packet.getX());
        roomPlayer.setBaitY(packet.getY());
        FishManager.getInstance().registerBaitPosition(roomPlayer.getBaitX(), roomPlayer.getBaitY());
        FishManager.getInstance().frightenFishes(room.getRoomId(), roomPlayer.getBaitX(), roomPlayer.getBaitY());

        SMSGSetBaitLocation smsgSetBaitLocation = SMSGSetBaitLocation.builder()
                .playerPosition(roomPlayer.getPosition())
                .x(packet.getX())
                .z(packet.getZ())
                .y(packet.getY())
                .build();
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(smsgSetBaitLocation, connection);
    }
}
