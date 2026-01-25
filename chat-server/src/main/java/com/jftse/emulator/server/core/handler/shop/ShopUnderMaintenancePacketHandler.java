package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.shop.CMSGShopMaintenance;
import com.jftse.server.core.shared.packets.shop.SMSGShopMaintenance;

@PacketId(CMSGShopMaintenance.PACKET_ID)
public class ShopUnderMaintenancePacketHandler implements PacketHandler<FTConnection, CMSGShopMaintenance> {
    @Override
    public void handle(FTConnection connection, CMSGShopMaintenance packet) {
        /*
         * result:
         * 0 - OK
         * -1 - shop under maintenance message
         *
         * unk0: unknown int
         */
        FTClient client = connection.getClient();
        int playerId = !client.hasPlayer() ? 0 : Math.toIntExact(client.getPlayer().getId());

        SMSGShopMaintenance response = SMSGShopMaintenance.builder().result((short) 0).playerId(playerId).build();
        connection.sendTCP(response);
    }
}
