package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.item.ItemFactory;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

@PacketOperationIdentifier(PacketOperations.C2SRoomLeave)
public class RoomLeaveRequestPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);
        ItemFactory.SetBackFromMatchplay(false);

        Packet answerPacket = new Packet(PacketOperations.S2CRoomLeaveAnswer);
        answerPacket.write(0);
        connection.sendTCP(answerPacket);
    }
}
