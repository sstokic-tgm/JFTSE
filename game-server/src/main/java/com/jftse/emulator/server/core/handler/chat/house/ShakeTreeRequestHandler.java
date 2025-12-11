package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.housing.FruitManager;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.chat.house.CMSGShakeTreeRequest;
import com.jftse.server.core.shared.packets.chat.house.SMSGShakeTreeFail;
import com.jftse.server.core.shared.packets.chat.house.SMSGShakeTreeResponse;
import com.jftse.server.core.thread.ThreadManager;

import java.util.concurrent.TimeUnit;

@PacketId(CMSGShakeTreeRequest.PACKET_ID)
public class ShakeTreeRequestHandler implements PacketHandler<FTConnection, CMSGShakeTreeRequest> {
    @Override
    public void handle(FTConnection connection, CMSGShakeTreeRequest shakeTreeRequestPacket) {
        FTClient client = connection.getClient();
        if (client == null)
            return;

        RoomPlayer roomPlayer = client.getRoomPlayer();
        if (roomPlayer == null)
            return;

        FruitManager fruitManager = client.getFruitManager();
        boolean availableFruits = fruitManager.init(shakeTreeRequestPacket.getXPos(), shakeTreeRequestPacket.getYPos());

        SMSGShakeTreeResponse responsePacket = SMSGShakeTreeResponse.builder()
                .position(roomPlayer.getPosition())
                .x(shakeTreeRequestPacket.getXPos())
                .y(shakeTreeRequestPacket.getYPos())
                .available(availableFruits)
                .build();
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(responsePacket, client.getConnection());

        if (!availableFruits) {
            ThreadManager.getInstance().schedule(() -> {
                SMSGShakeTreeFail failPacket = SMSGShakeTreeFail.builder()
                        .position(roomPlayer.getPosition())
                        .x(shakeTreeRequestPacket.getXPos())
                        .y(shakeTreeRequestPacket.getYPos())
                        .unk0((short) 1)
                        .build();
                GameManager.getInstance().sendPacketToAllClientsInSameRoom(failPacket, client.getConnection());
            }, 1, TimeUnit.SECONDS);
        }
    }
}
