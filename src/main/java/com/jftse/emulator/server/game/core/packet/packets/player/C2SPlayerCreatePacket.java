package com.jftse.emulator.server.game.core.packet.packets.player;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SPlayerCreatePacket extends Packet {
    private int playerId;
    private String nickname;
    private byte strength;
    private byte stamina;
    private byte dexterity;
    private byte willpower;
    private byte statusPoints;
    private byte level;

    public C2SPlayerCreatePacket(Packet packet) {
        super(packet);

        this.playerId = this.readInt();
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
