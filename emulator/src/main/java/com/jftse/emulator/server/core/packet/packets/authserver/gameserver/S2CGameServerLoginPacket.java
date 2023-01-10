package com.jftse.emulator.server.core.packet.packets.authserver.gameserver;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGameServerLoginPacket extends Packet {
    public S2CGameServerLoginPacket(char result, byte unk0) {
        super(PacketOperations.S2CGameLoginData.getValueAsChar());
        this.write(result);
        this.write(unk0);
    }
}
