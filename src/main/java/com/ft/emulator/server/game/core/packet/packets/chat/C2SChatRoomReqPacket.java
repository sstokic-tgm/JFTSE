package com.ft.emulator.server.game.core.packet.packets.chat;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SChatRoomReqPacket extends Packet {

    private byte type;
    private String message;

    public C2SChatRoomReqPacket(Packet packet) {

	super(packet);

	this.type = this.readByte();
	this.message = this.readUnicodeString();
    }
}