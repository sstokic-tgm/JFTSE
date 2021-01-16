package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class C2SMatchplayPlayerIdsInSessionPacket extends Packet {
    private List<Integer> playerIds;
    private byte unk0;
    private byte unk1;
    private byte unk2;
    private byte unk3;
    private byte unk4;

    public C2SMatchplayPlayerIdsInSessionPacket(Packet packet) {
        super(packet);

        this.playerIds = new ArrayList<>();
        playerIds.add(packet.readInt());
        this.unk0 = packet.readByte();
        playerIds.add(packet.readInt());
        this.unk1 = packet.readByte();
        playerIds.add(packet.readInt());
        this.unk2 = packet.readByte();
        playerIds.add(packet.readInt());
        this.unk3 = packet.readByte();
        this.unk4 = packet.readByte();
    }
}
