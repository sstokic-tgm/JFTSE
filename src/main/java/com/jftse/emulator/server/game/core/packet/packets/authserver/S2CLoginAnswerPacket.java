package com.jftse.emulator.server.game.core.packet.packets.authserver;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CLoginAnswerPacket extends Packet {
    public final static short SUCCESS = 0;
    public final static short ACCOUNT_INVALID_PASSWORD = -1;
    public final static short ACCOUNT_ALREADY_LOGGED_IN = -2;
    public final static short ACCOUNT_EXPIRED_USER_ID = -3;
    public final static short ACCOUNT_INVALID_USER_ID = -4;
    public final static short ACCOUNT_BLOCKED_USER_ID = -6;
    public final static short INVAILD_VERSION = -62;

    public S2CLoginAnswerPacket(short result) {
        super(PacketID.S2CLoginAnswerPacket);
        this.write(result);
    }
}
