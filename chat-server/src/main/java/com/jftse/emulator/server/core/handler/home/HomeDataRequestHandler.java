package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.HomeService;
import com.jftse.server.core.shared.packets.home.CMSGHomeData;

@PacketId(CMSGHomeData.PACKET_ID)
public class HomeDataRequestHandler implements PacketHandler<FTConnection, CMSGHomeData> {
    private final HomeService homeService;

    public HomeDataRequestHandler() {
        this.homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public void handle(FTConnection connection, CMSGHomeData packet) {
        FTClient client = connection.getClient();
        Account account = client.getAccount();

        AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);
    }
}
