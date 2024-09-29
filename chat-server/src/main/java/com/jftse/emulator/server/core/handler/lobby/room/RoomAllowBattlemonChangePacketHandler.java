package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomAllowBattlemonChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomAllowBattlemonChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomAllowBattlemonChange)
public class RoomAllowBattlemonChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomAllowBattlemonChangeRequestPacket changeRoomAllowBattlemonRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomAllowBattlemonRequestPacket = new C2SRoomAllowBattlemonChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Room room = client.getActiveRoom();
        if (room != null) {
            byte allowBattlemon = changeRoomAllowBattlemonRequestPacket.getAllowBattlemon();
            // disable battlemon
            synchronized (room) {
                room.setAllowBattlemon(allowBattlemon);
            }

            S2CRoomAllowBattlemonChangeAnswerPacket roomAllowBattlemonChangeAnswerPacket = new S2CRoomAllowBattlemonChangeAnswerPacket(room.getAllowBattlemon());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(roomAllowBattlemonChangeAnswerPacket, client.getConnection());
        }
    }
}
