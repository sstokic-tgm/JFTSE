package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.core.life.room.RoomPlayer;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class S2CGameSetNameColorAndRemoveBlackBar extends Packet {
    public S2CGameSetNameColorAndRemoveBlackBar(Room room) {
        super(PacketOperations.S2CGameSetNameColorAndRemoveBlackBar.getValue());

        if (room == null) {
            this.write((char) 0);
        } else {
            final ConcurrentLinkedDeque<RoomPlayer> roomPlayerList = room.getRoomPlayerList();
            List<RoomPlayer> activePlayers = roomPlayerList.stream()
                    .filter(x -> x.getPosition() < 4)
                    .collect(Collectors.toList());

            this.write((char) activePlayers.size());
            for (RoomPlayer roomPlayer : activePlayers) {
                this.write(roomPlayer.getPosition());
                this.write(roomPlayer.getPosition());
            }
        }
    }
}