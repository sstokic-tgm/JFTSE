package com.jftse.emulator.server.core.packets.item;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerAnnouncePacket extends Packet {
    private int playerPocketId;
    private byte textSize;
    private byte textColor;
    private String message;

    public C2SPlayerAnnouncePacket(Packet packet) {
        super(packet);

        this.playerPocketId = this.readInt();
        this.textSize = this.readByte();
        this.textColor = this.readByte();
        this.message = this.readUnicodeString();
    }
}
