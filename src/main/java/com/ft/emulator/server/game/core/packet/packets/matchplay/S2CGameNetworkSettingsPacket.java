package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CGameNetworkSettingsPacket extends Packet {
    public S2CGameNetworkSettingsPacket(Room room) {
        super(PacketID.S2CGameNetworkSettings);
        this.write("127.0.0.1");
        this.write((char)5896);

        List<RoomPlayer> roomPlayer = room.getRoomPlayerList();
        int clientsInRoomSize = roomPlayer.size();
        int maxClientsInRoom = room.getPlayers();
        int missingClientsCount = maxClientsInRoom - clientsInRoomSize;

        roomPlayer.forEach(rp -> {
            this.write(Math.toIntExact(rp.getPlayer().getId()));
        });

        for(int i = 1; i <= missingClientsCount; i++) {
            this.write(0);
        }
    }
}
