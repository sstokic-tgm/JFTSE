package com.jftse.emulator.server.game.core.packet.packets.lobby.room;

import com.jftse.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2SRoomCreateRequestPacket extends Packet {
    private String roomName;
    private byte allowBattlemon;
    private byte mode;
    private byte rule;
    private byte players;
    private boolean isPrivate;
    private byte unk1;
    private boolean skillFree;
    private boolean quickSlot;
    private byte levelRange;
    private char bettingType;
    private int bettingAmount;
    private int ball;
    private String password;

    public C2SRoomCreateRequestPacket(Packet packet) {
        super(packet);

        this.roomName = this.readUnicodeString();
        this.allowBattlemon = this.readByte();
        this.mode = this.readByte();
        this.rule = this.readByte();
        this.players = this.readByte();
        this.isPrivate = this.readByte() == 1;
        this.unk1 = this.readByte();
        this.skillFree = this.readByte() == 1;
        this.quickSlot = this.readByte() == 1;
        this.levelRange = this.readByte();
        this.bettingType = this.readChar();
        this.bettingAmount = this.readInt();
        this.ball = this.readInt();
        if (this.isPrivate)
            this.password = this.readUnicodeString();
    }
}