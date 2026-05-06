package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

import java.util.List;

public class S2CClearInventoryPacket extends Packet {
    public S2CClearInventoryPacket(List<PlayerPocket> playerPocketList) {
        super(PacketOperations.S2CClearInventoryData);

        this.write((short) playerPocketList.size());
        playerPocketList.stream().map(pp -> pp.getId().intValue()).forEach(this::write);
    }
}
