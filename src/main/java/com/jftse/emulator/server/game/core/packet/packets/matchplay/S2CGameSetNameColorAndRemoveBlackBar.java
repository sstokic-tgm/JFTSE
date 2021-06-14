package com.jftse.emulator.server.game.core.packet.packets.matchplay;

import com.jftse.emulator.server.game.core.matchplay.room.Room;
import com.jftse.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CGameSetNameColorAndRemoveBlackBar extends Packet {
    public S2CGameSetNameColorAndRemoveBlackBar(Room room) {
        super(PacketID.S2CGameSetNameColorAndRemoveBlackBar);

        if (room == null) {
            this.write((char) 0);
        } else {
            List<RoomPlayer> roomPlayerList = room.getRoomPlayerList();

            this.write((char) roomPlayerList.size());
            for (RoomPlayer roomPlayer : roomPlayerList) {
                this.write(roomPlayer.getPosition());
                this.write(roomPlayer.getPosition());
            }
        }
    }
}