package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FishManager;
import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.CMSGSetBaitLocationPacket;
import com.jftse.emulator.server.core.packets.chat.house.SMSGSetBaitLocationPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.CMSG_SetBaitLocation)
public class SetBaitLocationHandler extends AbstractPacketHandler {
    private CMSGSetBaitLocationPacket packet;

    @Override
    public boolean process(Packet packet) {
        this.packet = new CMSGSetBaitLocationPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
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

        SMSGSetBaitLocationPacket setBaitLocationPacket = new SMSGSetBaitLocationPacket(roomPlayer.getPosition(), packet.getX(), packet.getZ(), packet.getY());
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(setBaitLocationPacket, (FTConnection) connection);
    }
}
