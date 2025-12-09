package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.shared.packets.home.CMSGLoadHomeItems;

import java.util.List;

@PacketId(CMSGLoadHomeItems.PACKET_ID)
public class HomeItemsLoadRequestPacketHandler implements PacketHandler<FTConnection, CMSGLoadHomeItems> {
    private final HomeService homeService;

    public HomeItemsLoadRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGLoadHomeItems packet) {
        FTClient client = connection.getClient();
        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(homeInventoryList);
        connection.sendTCP(homeItemsLoadAnswerPacket);
    }
}
