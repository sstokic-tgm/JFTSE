package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CRoomAllowBattlemonChangeAnswerPacket extends Packet {
    public S2CRoomAllowBattlemonChangeAnswerPacket(byte allowBattlemonChange) {
        super(PacketOperations.S2CRoomAllowBattlemonChange);

        this.write(allowBattlemonChange);
    }
}
