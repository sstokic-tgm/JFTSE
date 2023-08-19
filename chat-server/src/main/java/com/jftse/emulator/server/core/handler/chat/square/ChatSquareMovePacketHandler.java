package com.jftse.emulator.server.core.handler.chat.square;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.square.C2SChatSquareMovePacket;
import com.jftse.emulator.server.core.packets.chat.square.S2CChatSquareMovePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SChatSquareMove)
public class ChatSquareMovePacketHandler extends AbstractPacketHandler {
    private C2SChatSquareMovePacket chatSquareMovePacket;

    @Override
    public boolean process(Packet packet) {
        chatSquareMovePacket = new C2SChatSquareMovePacket(packet);
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

        S2CChatSquareMovePacket answerSquareMovePacket = new S2CChatSquareMovePacket(roomPlayer.getPosition(), chatSquareMovePacket.getUnk1(), chatSquareMovePacket.getX2(), chatSquareMovePacket.getY2());
        roomPlayer.getLastSquareMovePacket().set(chatSquareMovePacket);

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerSquareMovePacket, client.getConnection());
    }
}
