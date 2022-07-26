package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildLeaveRequestPacket extends Packet {

    public C2SGuildLeaveRequestPacket(Packet packet) {
        super(packet);
    }
}