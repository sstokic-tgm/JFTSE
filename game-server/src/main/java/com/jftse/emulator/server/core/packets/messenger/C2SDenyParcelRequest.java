package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SDenyParcelRequest extends Packet {
    private Integer parcelId;

    public C2SDenyParcelRequest(Packet packet) {
        super(packet);

        this.parcelId = packet.readInt();
    }
}
