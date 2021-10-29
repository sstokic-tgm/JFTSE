package com.jftse.emulator.server.core.packet.packets.messaging;

import com.jftse.emulator.server.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRemoveParcelFromListPacket extends Packet {
    public S2CRemoveParcelFromListPacket(Integer parcelId) {
        super(PacketID.S2CRemoveParcelFromListAnswer);

        this.write(parcelId);
    }
}
