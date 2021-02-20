package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerStatusPointChangePacket extends Packet {
    private byte strength;
    private byte stamina;
    private byte dexterity;
    private byte willpower;
    private byte statusPoints;

    public C2SPlayerStatusPointChangePacket(Packet packet) {
        super(packet);

        this.strength = this.readByte();
        this.stamina = this.readByte();
        this.dexterity = this.readByte();
        this.willpower = this.readByte();
        this.statusPoints = this.readByte();
    }
}
