package com.ft.emulator.server.game.server.packets.gameserver;

import com.ft.emulator.server.game.server.Packet;
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