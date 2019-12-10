package com.ft.emulator.server.game.server.packets.inventory;

import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

public class S2CInventorySellItemCheckAnswerPacket extends Packet {

    public final static byte SUCCESS = 0;
    public final static byte NO_ITEM = -1;
    public final static byte IMPOSSIBLE_ITEM = -2;

    public S2CInventorySellItemCheckAnswerPacket(byte status) {

        super(PacketID.S2CInventorySellItemCheckAnswer);

        this.write(status);
    }
}