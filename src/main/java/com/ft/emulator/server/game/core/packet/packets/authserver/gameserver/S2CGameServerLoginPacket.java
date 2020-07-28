package com.ft.emulator.server.game.core.packet.packets.authserver.gameserver;

import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

public class S2CGameServerLoginPacket extends Packet {
    public S2CGameServerLoginPacket(char result, byte unk0) {
        super(PacketID.S2CGameLoginData);
        this.write(result);
        this.write(unk0);
    }
}
