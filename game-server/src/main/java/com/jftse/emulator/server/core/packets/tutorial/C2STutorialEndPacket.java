package com.jftse.emulator.server.core.packets.tutorial;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2STutorialEndPacket extends Packet {
    private byte result;

    public C2STutorialEndPacket(Packet packet) {
        super(packet);

        this.result = this.readByte();
    }
}
