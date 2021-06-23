package com.jftse.emulator.server.game.core.packet.packets.messaging;

import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class S2CSendParcelAnswerPacket extends Packet {
    public S2CSendParcelAnswerPacket(short status) {
        super(PacketID.S2CSendParcelAnswer);

        this.write(status);
    }
}
