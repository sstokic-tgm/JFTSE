package com.jftse.emulator.server.core.packets.shop;

import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

public class S2CShopUnderMaintenancePacket extends Packet {
    public S2CShopUnderMaintenancePacket(short result, int playerId) {
        super(PacketOperations.S2CShopUnderS2CShopUnderMaintenancePacket);

        this.write(result);
        this.write(playerId);
    }
}
