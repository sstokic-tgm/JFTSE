package com.jftse.emulator.server.core.packets.enchant;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SEnchantAnnouncePacket extends Packet {
    private byte textSize;
    private byte textColor;
    private String message;

    public C2SEnchantAnnouncePacket(Packet packet) {
        super(packet);

        this.textSize = this.readByte();
        this.textColor = this.readByte();
        this.message = this.readUnicodeString();
    }
}
