package com.ft.emulator.server.game.server.packets.gameserver;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CGameServerAnswerPacket extends Packet {

    public S2CGameServerAnswerPacket(byte requestType) {

        super(PacketID.S2CGameAnswerData);

        this.write(requestType);
        this.write((byte)0);
    }
}