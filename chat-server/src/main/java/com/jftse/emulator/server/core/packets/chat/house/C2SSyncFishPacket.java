package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SSyncFishPacket extends Packet {
    private short fishId;

    public C2SSyncFishPacket(Packet packet) {
        super(packet);

        this.fishId = packet.readShort();
    }
}
