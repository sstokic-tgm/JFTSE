package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SCharacterCreatePacket extends Packet {

    private int characterId;
    private String nickname;
    private byte strength;
    private byte stamina;
    private byte dexterity;
    private byte willpower;
    private byte statusPoints;
    private byte level;

    public C2SCharacterCreatePacket(Packet packet) {

        super(packet);

        this.characterId = this.readInt();
        this.nickname = this.readUnicodeString();
        this.nickname = getNickname().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
        this.strength = this.readByte();
        this.stamina = this.readByte();
        this.dexterity = this.readByte();
        this.willpower = this.readByte();
        this.statusPoints = this.readByte();
        this.level = this.readByte();
    }
}