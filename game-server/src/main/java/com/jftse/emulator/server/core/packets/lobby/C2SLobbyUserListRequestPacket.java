package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLobbyUserListRequestPacket extends Packet {
    private byte page;
    private short refresh;

    public C2SLobbyUserListRequestPacket(Packet packet) {
        super(packet);

        this.page = this.readByte();
        this.refresh = this.readShort();
    }
}
