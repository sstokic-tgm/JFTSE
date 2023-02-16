package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SFirstPlayerPacket extends Packet {
    private byte playerType;

    public C2SFirstPlayerPacket(Packet packet) {
        super(packet);

        this.playerType = this.readByte();
    }
}
