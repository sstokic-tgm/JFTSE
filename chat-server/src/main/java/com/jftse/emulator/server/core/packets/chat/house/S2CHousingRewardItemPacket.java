package com.jftse.emulator.server.core.packets.chat.house;

import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CHousingRewardItemPacket extends Packet {
    public S2CHousingRewardItemPacket(PlayerPocket playerPocket) {
        super(PacketOperations.S2CHousingRewardItem);

        this.write(Math.toIntExact(playerPocket.getId()));
        this.write(playerPocket.getItemIndex());
        this.write(playerPocket.getItemCount());
    }
}
