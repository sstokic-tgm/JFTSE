package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeFailPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SShakeTreeFail)
public class ShakeTreeFailHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
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
        if (fruitManager == null) {
            return;
        }

        FruitTree fruitTree = fruitManager.getFruitTree();
        if (fruitTree == null) {
            return;
        }

        S2CShakeTreeFailPacket shakeTreeFailPacket = new S2CShakeTreeFailPacket(roomPlayer.getPosition(), fruitTree, (short) 1);
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeFailPacket, client.getConnection());
    }
}
