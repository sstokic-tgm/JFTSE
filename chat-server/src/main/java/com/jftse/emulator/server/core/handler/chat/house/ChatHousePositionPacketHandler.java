package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SChatHousePositionPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CChatHousePositionPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SChatHousePosition)
public class ChatHousePositionPacketHandler extends AbstractPacketHandler {
    private C2SChatHousePositionPacket chatHousePositionPacket;

    @Override
    public boolean process(Packet packet) {
        chatHousePositionPacket = new C2SChatHousePositionPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        if (roomPlayer.isFitting())
            return;

        S2CChatHousePositionPacket answerHousePositionPacket = new S2CChatHousePositionPacket(roomPlayer.getPosition(), chatHousePositionPacket.getLevel(), chatHousePositionPacket.getX(), chatHousePositionPacket.getY());
        roomPlayer.setLastX(chatHousePositionPacket.getX());
        roomPlayer.setLastY(chatHousePositionPacket.getY());
        roomPlayer.setLastMapLayer(chatHousePositionPacket.getLevel());

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerHousePositionPacket, client.getConnection());
    }
}
