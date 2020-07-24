package com.ft.emulator.server.game.core.packet.packets.authserver.gameserver;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGameServerLoginPacket extends Packet {

    private int playerId;

    public C2SGameServerLoginPacket(Packet packet) {

	super(packet);

	this.playerId = this.readInt();
    }
}