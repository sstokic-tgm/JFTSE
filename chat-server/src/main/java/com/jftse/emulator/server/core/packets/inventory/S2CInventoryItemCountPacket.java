package com.jftse.emulator.server.core.packets.inventory;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CInventoryItemCountPacket extends Packet {
    public S2CInventoryItemCountPacket(PlayerPocket playerPocket) {
        super(PacketOperations.S2CInventoryItemCount);

        this.write(playerPocket.getId().intValue());
        this.write(playerPocket.getItemCount());
    }
}
