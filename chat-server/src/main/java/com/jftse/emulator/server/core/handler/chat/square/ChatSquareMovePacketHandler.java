package com.jftse.emulator.server.core.handler.chat.square;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.square.CMSGChatSquareMove;
import com.jftse.server.core.shared.packets.chat.square.SMSGChatSquareMove;

@PacketId(CMSGChatSquareMove.PACKET_ID)
public class ChatSquareMovePacketHandler implements PacketHandler<FTConnection, CMSGChatSquareMove> {
    @Override
    public void handle(FTConnection connection, CMSGChatSquareMove chatSquareMovePacket) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        if (roomPlayer.isFitting())
            return;

        SMSGChatSquareMove answerSquareMovePacket = SMSGChatSquareMove.builder()
                .position(roomPlayer.getPosition())
                .unk0(chatSquareMovePacket.getUnk0())
                .x(chatSquareMovePacket.getX2())
                .y(chatSquareMovePacket.getY2())
                .build();
        roomPlayer.setLastX(chatSquareMovePacket.getX2());
        roomPlayer.setLastY(chatSquareMovePacket.getY2());

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerSquareMovePacket, client.getConnection());
    }
}
