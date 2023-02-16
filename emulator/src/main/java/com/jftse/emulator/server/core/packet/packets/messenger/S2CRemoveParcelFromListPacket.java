package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CRemoveParcelFromListPacket extends Packet {
    public S2CRemoveParcelFromListPacket(Integer parcelId) {
        super(PacketOperations.S2CRemoveParcelFromListAnswer.getValueAsChar());

        this.write(parcelId);
    }
}
