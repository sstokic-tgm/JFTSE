package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomListRequestPacket extends Packet {
    private int roomTypeTab;
    private short roomOffset;
    private byte unk0;
    private byte direction;

    public C2SRoomListRequestPacket(Packet packet) {
        super(packet);

        this.roomTypeTab = this.readInt();
        this.roomOffset = this.readShort();
        this.unk0 = this.readByte();
        this.direction = this.readByte();
    }
}