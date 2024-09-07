package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.packets.shop.S2CShopUnderMaintenancePacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;

@PacketOperationIdentifier(PacketOperations.C2SShopUnderS2CShopUnderMaintenancePacket)
public class ShopUnderMaintenancePacketHandler extends AbstractPacketHandler {
    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        /*
         * result:
         * 0 - OK
         * -1 - shop under maintenance message
         *
         * unk0: unknown int
         */
        FTClient client = (FTClient) connection.getClient();
        int playerId = client.getActivePlayerId() == null ? 0 : Math.toIntExact(client.getActivePlayerId());

        S2CShopUnderMaintenancePacket shopUnderMaintenancePacket = new S2CShopUnderMaintenancePacket((short) 0, playerId);
        connection.sendTCP(shopUnderMaintenancePacket);
    }
}
