package com.jftse.emulator.server.core.packets.lottery;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SOpenGachaReqPacket extends Packet {

    private int playerPocketId;
    private int productIndex;

    public C2SOpenGachaReqPacket(Packet packet) {
        super(packet);

        this.playerPocketId = this.readInt();
        this.productIndex = this.readInt();
    }
}
