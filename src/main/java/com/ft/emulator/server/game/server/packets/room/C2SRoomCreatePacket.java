package com.ft.emulator.server.game.server.packets.room;

import com.ft.emulator.server.game.server.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomCreatePacket extends Packet {

    private String name;
    private byte type;
    private byte gameMode;
    private byte rule;
    private byte players;
    private boolean isPrivate;
    private byte unknown;
    private boolean skillFree;
    private boolean quickSlot;
    private byte levelRange;
    private char bettingType;
    private int bettingAmount;
    private int ball;
    private String password;

    public C2SRoomCreatePacket(Packet packet) {

        super(packet);

        this.name = this.readUnicodeString().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
        this.type = this.readByte();
        this.gameMode = this.readByte();
        this.rule = this.readByte();
        this.players = this.readByte();
        this.isPrivate = this.readByte() == 1;
        this.unknown = this.readByte();
        this.skillFree = this.readByte() == 1;
        this.quickSlot = this.readByte() == 1;
        this.levelRange = this.readByte();
        this.bettingType = this.readChar();
        this.bettingAmount = this.readInt();
        this.ball = this.readInt();

        if(this.isPrivate) {
            this.password = this.readUnicodeString().trim().replaceAll("[^a-zA-Z0-9\\s+]", "");
	}
    }
}