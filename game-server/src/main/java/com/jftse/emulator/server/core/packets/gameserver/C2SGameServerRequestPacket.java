package com.jftse.emulator.server.core.packets.gameserver;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGameServerRequestPacket extends Packet {
    private byte requestType;

    public C2SGameServerRequestPacket(Packet packet) {
        super(packet);
        this.requestType = this.readByte();
    }
}
