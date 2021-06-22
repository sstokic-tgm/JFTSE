package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CAcceptParcelAnswer extends Packet {
    public S2CAcceptParcelAnswer(short status) {
        super(PacketID.S2CAcceptParcelAnswer);

        this.write(status);
    }
}
