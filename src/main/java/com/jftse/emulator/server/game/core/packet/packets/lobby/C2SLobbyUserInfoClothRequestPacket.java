package com.jftse.emulator.server.game.core.packet.packets.lobby;

import com.jftse.emulator.server.networking.packet.Packet;
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