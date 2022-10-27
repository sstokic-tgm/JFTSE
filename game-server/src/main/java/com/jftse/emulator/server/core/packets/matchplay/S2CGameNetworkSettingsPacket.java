package com.jftse.emulator.server.core.packets.matchplay;

import com.jftse.emulator.server.core.life.room.Room;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;

import java.util.List;

public class S2CGameNetworkSettingsPacket extends Packet {
    public S2CGameNetworkSettingsPacket(String host, int port, Room room, List<FTClient> clientsInRoom) {
        super(PacketOperations.S2CGameNetworkSettings.getValue());

        this.write(host);
        this.write((char) port);

        this.write((int) room.getRoomId()); // session id actually

        int clientsInRoomSize = clientsInRoom.size();
        int maxClientsInRoom = 4;
        int missingClientsCount = maxClientsInRoom - clientsInRoomSize;

        clientsInRoom.forEach(c -> {
            if (c.getPlayer() != null)
                this.write(Math.toIntExact(c.getPlayer().getId()));
        });

        for (int i = 1; i <= missingClientsCount; i++) {
            this.write(0);
        }
    }
}
