package com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGameServerAnswerPacket extends Packet {
    public S2CGameServerAnswerPacket(byte requestType, byte unk0) {
        super(PacketID.S2CGameAnswerData);
        this.write(requestType);
        this.write(unk0);
    }
}
