package com.jftse.emulator.server.core.packet.packets.matchplay;

import com.jftse.emulator.server.core.matchplay.room.Room;
import com.jftse.emulator.server.core.matchplay.room.RoomPlayer;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.stream.Collectors;

public class S2CGameSetNameColorAndRemoveBlackBar extends Packet {
    public S2CGameSetNameColorAndRemoveBlackBar(Room room) {
        super(PacketOperations.S2CGameSetNameColorAndRemoveBlackBar.getValueAsChar());

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