package com.ft.emulator.server.game.core.packet.packets.lobby;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SLobbyUserInfoRequestPacket extends Packet {
    private int playerId;
    private String playerName;

    public C2SLobbyUserInfoRequestPacket(Packet packet) {
        super(packet);

        this.playerId = this.readInt();
        this.playerName = this.readUnicodeString();
    }
}