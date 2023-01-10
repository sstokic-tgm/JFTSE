package com.jftse.emulator.server.core.packets.guild;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildCreateRequestPacket extends Packet {
    private String name;
    private String introduction;
    private boolean isPublic;
    private byte levelRestriction;
    private byte allowedCharacterTypeCount;
    private Byte[] allowedCharacterType;

    public C2SGuildCreateRequestPacket(Packet packet) {
        super(packet);

        this.name = this.readUnicodeString();
        this.introduction = this.readUnicodeString();
        this.isPublic = this.readBoolean();
        this.levelRestriction = this.readByte();
        this.allowedCharacterTypeCount = this.readByte();
        this.allowedCharacterType = new Byte[this.allowedCharacterTypeCount];

        for (int i = 0; i < this.allowedCharacterTypeCount; i++)
            this.allowedCharacterType[i] = this.readByte();
    }
}
