package com.jftse.emulator.server.core.packets.messenger;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRemoveParcelFromListPacket extends Packet {
    public S2CRemoveParcelFromListPacket(Integer parcelId) {
        super(PacketOperations.S2CRemoveParcelFromListAnswer);

        this.write(parcelId);
    }
}
