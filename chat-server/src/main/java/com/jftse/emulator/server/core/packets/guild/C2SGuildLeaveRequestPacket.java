package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildLeaveRequestPacket extends Packet {

    public C2SGuildLeaveRequestPacket(Packet packet) {
        super(packet);
    }
}