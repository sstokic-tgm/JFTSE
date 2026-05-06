package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.housing.FruitTree;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGShakeTreeFail;
import com.jftse.server.core.shared.packets.chat.house.SMSGShakeTreeFail;

@PacketId(CMSGShakeTreeFail.PACKET_ID)
public class ShakeTreeFailHandler implements PacketHandler<FTConnection, CMSGShakeTreeFail> {
    @Override
    public void handle(FTConnection connection, CMSGShakeTreeFail packet) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        FruitManager fruitManager = client.getFruitManager();

        FruitTree fruitTree = fruitManager.getFruitTree();
        if (fruitTree == null) {
            return;
        }

        SMSGShakeTreeFail shakeTreeFail = SMSGShakeTreeFail.builder()
                .position(roomPlayer.getPosition())
                .x(fruitTree.getX())
                .y(fruitTree.getY())
                .unk0((short) 1)
                .build();
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeFail, client.getConnection());
    }
}
