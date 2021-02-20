package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

public class S2CGameSetNameColor extends Packet {
    public S2CGameSetNameColor(RoomPlayer roomPlayer) {
        super(PacketID.S2CGameSetNameColors);

        this.write((char) roomPlayer.getPosition());

        boolean isRedTeam = roomPlayer.getPosition() == 0 || roomPlayer.getPosition() == 2;
        this.write((char) (isRedTeam ? 1 : 0));
        this.write((char) (!isRedTeam ? 1 : 0));
    }
}