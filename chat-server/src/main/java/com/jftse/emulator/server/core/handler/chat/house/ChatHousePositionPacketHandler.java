package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGChatHousePosition;
import com.jftse.server.core.shared.packets.chat.house.SMSGChatHousePosition;

@PacketId(CMSGChatHousePosition.PACKET_ID)
public class ChatHousePositionPacketHandler implements PacketHandler<FTConnection, CMSGChatHousePosition> {
    @Override
    public void handle(FTConnection connection, CMSGChatHousePosition chatHousePositionPacket) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        if (roomPlayer.isFitting())
            return;

        SMSGChatHousePosition answerHousePosition = SMSGChatHousePosition.builder()
                .position(roomPlayer.getPosition())
                .level(chatHousePositionPacket.getLevel())
                .x(chatHousePositionPacket.getX())
                .y(chatHousePositionPacket.getY())
                .build();
        roomPlayer.setLastX(chatHousePositionPacket.getX());
        roomPlayer.setLastY(chatHousePositionPacket.getY());
        roomPlayer.setLastMapLayer(chatHousePositionPacket.getLevel());

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerHousePosition, connection);
    }
}
