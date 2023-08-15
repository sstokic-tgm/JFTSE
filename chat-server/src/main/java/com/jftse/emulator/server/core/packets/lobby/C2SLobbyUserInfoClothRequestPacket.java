package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLobbyUserInfoClothRequestPacket extends Packet {
    private int playerId;

    public C2SLobbyUserInfoClothRequestPacket(Packet packet) {
        super(packet);

        this.playerId = this.readInt();
    }
}