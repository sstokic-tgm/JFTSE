package com.ft.emulator.server.game.server.packets.character;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SCharacterStatusPointChangePacket extends Packet {

    private byte strength;
    private byte stamina;
    private byte dexterity;
    private byte willpower;
    private byte statusPoints;

    public C2SCharacterStatusPointChangePacket(Packet packet) {

        super(packet);

        this.strength = this.readByte();
        this.stamina = this.readByte();
        this.dexterity = this.readByte();
        this.willpower = this.readByte();
        this.statusPoints = this.readByte();
    }
}