package com.jftse.emulator.server.game.core.packet.packets.authserver.gameserver;

import com.jftse.emulator.server.networking.packet.Packet;
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
