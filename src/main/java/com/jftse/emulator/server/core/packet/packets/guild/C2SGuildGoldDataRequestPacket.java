package com.jftse.emulator.server.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildGoldDataRequestPacket extends Packet {

    public C2SGuildGoldDataRequestPacket(Packet packet) {
        super(packet);
    }
}