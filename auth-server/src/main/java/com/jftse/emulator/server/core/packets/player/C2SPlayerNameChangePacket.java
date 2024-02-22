package com.jftse.emulator.server.core.packets.player;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerNameChangePacket extends Packet {
    private int playerId;
    private String playerName;
    private String newPlayerName;

    public C2SPlayerNameChangePacket(Packet packet) {
        super(packet);

        this.playerId = packet.readInt();
        this.playerName = packet.readUnicodeString();
        this.newPlayerName = packet.readUnicodeString();
    }
}
