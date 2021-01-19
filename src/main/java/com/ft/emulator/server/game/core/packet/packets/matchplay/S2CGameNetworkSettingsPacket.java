package com.ft.emulator.server.game.core.packet.packets.matchplay;

import com.ft.emulator.server.game.core.matchplay.room.Room;
import com.ft.emulator.server.game.core.matchplay.room.RoomPlayer;
import com.ft.emulator.server.game.core.packet.PacketID;
import com.ft.emulator.server.networking.packet.Packet;
import com.ft.emulator.server.shared.module.Client;

import java.util.List;

public class S2CGameNetworkSettingsPacket extends Packet {
    public S2CGameNetworkSettingsPacket(Room room, List<Client> clientsInRoom) {
        super(PacketID.S2CGameNetworkSettings);

        this.write("127.0.0.1");
        this.write((char) 5896);

        this.write((int) room.getRoomId()); // session id actually

        int clientsInRoomSize = clientsInRoom.size();
        int maxClientsInRoom = 4;
        int missingClientsCount = maxClientsInRoom - clientsInRoomSize;

        clientsInRoom.forEach(c -> this.write(Math.toIntExact(c.getActivePlayer().getId())));

        for (int i = 1; i <= missingClientsCount; i++) {
            this.write(0);
        }
    }
}
