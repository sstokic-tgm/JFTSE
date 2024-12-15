package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRequestLobbyOptionPacket extends Packet {
    private byte option;

    public C2SRequestLobbyOptionPacket(Packet packet) {
        super(packet);

        this.option = this.readByte();
    }
}
