package com.jftse.emulator.server.core.handler.game.lobby.room;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class RoomLeaveRequestPacketHandler extends AbstractHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        connection.getClient().setLobbyCurrentRoomListPage(-1);
        GameManager.getInstance().handleRoomPlayerChanges(connection, true);
        Packet answerPacket = new Packet(PacketOperations.S2CRoomLeaveAnswer.getValueAsChar());
        answerPacket.write(0);
        connection.sendTCP(answerPacket);
    }
}
