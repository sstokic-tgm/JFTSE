package com.jftse.emulator.server.core.packet.packets.messenger;

import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CCancelParcelSendingAnswer extends Packet {
    public S2CCancelParcelSendingAnswer(short status) {
        super(PacketOperations.S2CCancelParcelSendingAnswer.getValueAsChar());

        this.write(status);
    }
}
