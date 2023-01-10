package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.packets.home.S2CHomeItemsLoadAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.entities.database.model.home.HomeInventory;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.HomeService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SHomeItemsLoadReq)
public class HomeItemsLoadRequestPacketHandler extends AbstractPacketHandler {
    private final HomeService homeService;

    public HomeItemsLoadRequestPacketHandler() {
        homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        AccountHome accountHome = homeService.findAccountHomeByAccountId(client.getAccount().getId());
        List<HomeInventory> homeInventoryList = homeService.findAllByAccountHome(accountHome);

        S2CHomeItemsLoadAnswerPacket homeItemsLoadAnswerPacket = new S2CHomeItemsLoadAnswerPacket(homeInventoryList);
        connection.sendTCP(homeItemsLoadAnswerPacket);
    }
}
