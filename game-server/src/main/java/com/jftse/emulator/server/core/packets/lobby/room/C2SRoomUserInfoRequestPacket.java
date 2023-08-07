package com.jftse.emulator.server.core.packets.lobby.room;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomUserInfoRequestPacket extends Packet {
    private short position;
    private String nickname;

    public C2SRoomUserInfoRequestPacket(Packet packet) {
        super(packet);

        this.position = this.readShort();
        this.nickname = this.readUnicodeString();
    }
}
