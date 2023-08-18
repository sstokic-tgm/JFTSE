package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SChatHouseMovePacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CChatHouseMovePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SChatHouseMove)
public class ChatHouseMovePacketHandler extends AbstractPacketHandler {
    private C2SChatHouseMovePacket chatHouseMovePacket;

    @Override
    public boolean process(Packet packet) {
        chatHouseMovePacket = new C2SChatHouseMovePacket(packet);
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

        S2CChatHouseMovePacket answerHouseMovePacket = new S2CChatHouseMovePacket(roomPlayer.getPosition(), chatHouseMovePacket.getUnk1(), chatHouseMovePacket.getUnk2(), chatHouseMovePacket.getX(), chatHouseMovePacket.getY(), chatHouseMovePacket.getAnimationType(), chatHouseMovePacket.getUnk3());
        roomPlayer.getLastHouseMovePacket().set(answerHouseMovePacket);

        GameManager.getInstance().sendPacketToAllClientsInSameRoom(answerHouseMovePacket, client.getConnection());
    }
}
