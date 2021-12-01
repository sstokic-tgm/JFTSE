package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CAcceptParcelAnswer extends Packet {
    public S2CAcceptParcelAnswer(short status) {
        super(PacketOperations.S2CAcceptParcelAnswer.getValueAsChar());

        this.write(status);
    }
}
