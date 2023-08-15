package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomSkillFreeChangeAnswerPacket extends Packet {
    public S2CRoomSkillFreeChangeAnswerPacket(boolean isSkillFree) {
        super(PacketOperations.S2CRoomSkillFreeChange);

        this.write(isSkillFree);
    }
}
