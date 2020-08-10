package com.ft.emulator.server.game.core.packet.packets.lobby.room;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomListRequestPacket extends Packet {
    private int roomTypeTab;
    private char page;
    private byte unk0;
    private byte unk1;

    public C2SRoomListRequestPacket(Packet packet) {
        super(packet);

        this.roomTypeTab = this.readInt();
        this.page = this.readChar();
        this.unk0 = this.readByte();
        this.unk1 = this.readByte();
    }
}