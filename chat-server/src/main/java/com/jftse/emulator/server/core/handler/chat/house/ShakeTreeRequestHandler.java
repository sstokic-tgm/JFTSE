package com.jftse.emulator.server.core.handler.chat.house;

import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.chat.house.C2SShakeTreeRequestPacket;
import com.jftse.emulator.server.core.packets.chat.house.S2CShakeTreeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

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

        S2CShakeTreeAnswerPacket shakeTreeAnswerPacket = new S2CShakeTreeAnswerPacket(roomPlayer.getPosition(), shakeTreeRequestPacket.getXPos(), shakeTreeRequestPacket.getYPos(), (byte) 1);
        GameManager.getInstance().sendPacketToAllClientsInSameRoom(shakeTreeAnswerPacket, client.getConnection());
    }
}
