package com.jftse.emulator.server.core.packets.gameserver;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

public class S2CGameServerLoginPacket extends Packet {
    public S2CGameServerLoginPacket(char result, byte unk0) {
        super(PacketOperations.S2CGameLoginData);
        this.write(result);
        this.write(unk0);
    }
}
