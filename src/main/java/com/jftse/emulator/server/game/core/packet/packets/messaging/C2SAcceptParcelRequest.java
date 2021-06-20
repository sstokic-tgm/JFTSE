package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.networking.packet.Packet;
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
