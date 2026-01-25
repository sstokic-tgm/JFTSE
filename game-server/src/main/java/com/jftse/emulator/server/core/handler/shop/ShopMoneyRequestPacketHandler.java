package com.jftse.emulator.server.core.handler.shop;

import com.jftse.emulator.server.core.client.FTPlayer;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.shop.CMSGGetMoney;
import com.jftse.server.core.shared.packets.shop.SMSGSetMoney;

@PacketId(CMSGGetMoney.PACKET_ID)
public class ShopMoneyRequestPacketHandler implements PacketHandler<FTConnection, CMSGGetMoney> {
    @Override
    public void handle(FTConnection connection, CMSGGetMoney packet) {
        FTClient ftClient = connection.getClient();
        if (!ftClient.hasPlayer()) {
            return;
        }

        FTPlayer player = ftClient.getPlayer();

        SMSGSetMoney moneyPacket = SMSGSetMoney.builder()
                .ap(ftClient.getAp().get())
                .gold(player.getGold())
                .build();
        connection.sendTCP(moneyPacket);
    }
}
