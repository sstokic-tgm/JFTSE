package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomLeave)
public class RoomLeaveRequestPacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();

        if (!client.getIsJoiningOrLeavingRoom().compareAndSet(false, true)) {
            return;
        }

        client.setLobbyCurrentRoomListPage(-1);

        GameManager.getInstance().handleRoomPlayerChanges(client.getConnection(), true);

        Packet answerPacket = new Packet(PacketOperations.S2CRoomLeaveAnswer);
        answerPacket.write((short) 0);
        connection.sendTCP(answerPacket);

        client.getIsJoiningOrLeavingRoom().set(false);

        //GameManager.getInstance().handleChatLobbyJoin(client);
    }
}
