package com.jftse.emulator.server.core.packets.gameserver;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CGameServerAnswerPacket extends Packet {
    public S2CGameServerAnswerPacket(byte requestType, byte unk0) {
        super(PacketOperations.S2CGameAnswerData);
        this.write(requestType);
        this.write(unk0);
    }
}
