package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SCombineNowRecipeReqPacket extends Packet {
    private int playerPocketId;
    public C2SCombineNowRecipeReqPacket(Packet packet) {
        super(packet);

        this.playerPocketId = this.readInt();
    }
}
