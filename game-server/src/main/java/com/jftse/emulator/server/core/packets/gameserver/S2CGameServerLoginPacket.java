package com.jftse.emulator.server.core.packets.gameserver;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CGameServerLoginPacket extends Packet {
    public S2CGameServerLoginPacket(char result, byte unk0) {
        super(PacketOperations.S2CGameLoginData);
        this.write(result);
        this.write(unk0); // game server type
    }
}
