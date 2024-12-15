package com.jftse.emulator.server.core.packets.chat;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SFireCrackerReqPacket extends Packet {
    private int playerPocketId;
    private byte fireCrackerType;
    private short position;

    public C2SFireCrackerReqPacket(Packet packet) {
        super(packet);

        this.playerPocketId = this.readInt();
        this.fireCrackerType = this.readByte();
        this.position = this.readShort();
    }
}
