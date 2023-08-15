package com.jftse.emulator.server.core.packets.lobby;

import com.jftse.server.core.protocol.Packet;
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