package com.jftse.emulator.server.core.handler.lobby.room;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.manager.GameManager;
import com.jftse.emulator.server.core.packets.lobby.room.C2SRoomSkillFreeChangeRequestPacket;
import com.jftse.emulator.server.core.packets.lobby.room.S2CRoomSkillFreeChangeAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SRoomSkillFreeChange)
public class RoomSkillFreeChangePacketHandler extends AbstractPacketHandler {
    private C2SRoomSkillFreeChangeRequestPacket changeRoomSkillFreeRequestPacket;

    @Override
    public boolean process(Packet packet) {
        changeRoomSkillFreeRequestPacket = new C2SRoomSkillFreeChangeRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        FTClient ftClient = (FTClient) connection.getClient();
        Room room = ftClient.getActiveRoom();
        if (room != null) {
            synchronized (room) {
                room.setSkillFree(changeRoomSkillFreeRequestPacket.isSkillFree());
            }

            S2CRoomSkillFreeChangeAnswerPacket skillFreeChangeAnswerPacket = new S2CRoomSkillFreeChangeAnswerPacket(room.isSkillFree());
            GameManager.getInstance().sendPacketToAllClientsInSameRoom(skillFreeChangeAnswerPacket, ftClient.getConnection());
        }
    }
}
