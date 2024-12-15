package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPersonalBoardPacket extends Packet {
    private int playerPocketId;
    private String message;

    public C2SPersonalBoardPacket(Packet packet) {
        super(packet);

        this.playerPocketId = this.readInt();
        this.message = this.readUnicodeString();
    }
}
