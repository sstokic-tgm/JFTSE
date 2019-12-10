package com.ft.emulator.server.game.server.packets.gameserver;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGameServerLoginPacket extends Packet {

    private int characterId;

    public C2SGameServerLoginPacket(Packet packet) {

        super(packet);

        this.characterId = this.readInt();
    }
}