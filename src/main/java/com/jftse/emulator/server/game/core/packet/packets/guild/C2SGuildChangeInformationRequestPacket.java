package com.jftse.emulator.server.game.core.packet.packets.guild;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SGuildChangeInformationRequestPacket extends Packet {
    private String introduction;
    private boolean isPublic;
    private byte minLevel;
    private byte allowedCharacterTypeCount;
    private byte[] allowedCharacterType;

    public C2SGuildChangeInformationRequestPacket(Packet packet) {
        super(packet);

        this.introduction = this.readUnicodeString();
        this.isPublic = this.readBoolean();
        this.minLevel = this.readByte();
        this.allowedCharacterTypeCount = this.readByte();
        this.allowedCharacterType = new byte[this.allowedCharacterTypeCount];

        for (int i = 0; i < this.allowedCharacterTypeCount; i++)
            this.allowedCharacterType[i] = this.readByte();
    }
}