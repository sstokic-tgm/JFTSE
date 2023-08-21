package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SShakeTreeRequestPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeAnswerPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeFailPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.thread.ThreadManager;

import java.util.concurrent.TimeUnit;

@PacketOperationIdentifier(PacketOperations.C2SShakeTreeRequest)
public class ShakeTreeRequestHandler extends AbstractPacketHandler {
    private C2SShakeTreeRequestPacket shakeTreeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        shakeTreeRequestPacket = new C2SShakeTreeRequestPacket(packet);
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

        FruitManager fruitManager = client.getFruitManager();
        boolean availableFruits = fruitManager.init(shakeTreeRequestPacket.getXPos(), shakeTreeRequestPacket.getYPos());

        S2CShakeTreeAnswerPacket shakeTreeAnswerPacket = new S2CShakeTreeAnswerPacket(roomPlayer.getPosition(), shakeTreeRequestPacket.getXPos(), shakeTreeRequestPacket.getYPos(), availableFruits);
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeAnswerPacket, client.getConnection());

        if (!availableFruits) {
            ThreadManager.getInstance().schedule(() -> {
                S2CShakeTreeFailPacket shakeTreeFailPacket = new S2CShakeTreeFailPacket(roomPlayer.getPosition(), shakeTreeRequestPacket.getXPos(), shakeTreeRequestPacket.getYPos(), (short) 1);
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeFailPacket, client.getConnection());
            }, 1, TimeUnit.SECONDS);
        }
    }
}
