package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SAcceptParcelRequest extends Packet {
    private Integer parcelId;

    public C2SAcceptParcelRequest(Packet packet) {
        super(packet);

        this.parcelId = packet.readInt();
    }
}
