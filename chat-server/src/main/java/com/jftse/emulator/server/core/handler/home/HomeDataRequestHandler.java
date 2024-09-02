package com.jftse.emulator.server.core.handler.home;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.home.S2CHomeDataPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.account.Account;
import com.jftse.entities.database.model.home.AccountHome;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.HomeService;

@PacketOperationIdentifier(PacketOperations.C2SHomeData)
public class HomeDataRequestHandler extends AbstractPacketHandler {
    private final HomeService homeService;

    public HomeDataRequestHandler() {
        this.homeService = ServiceManager.getInstance().getHomeService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        Account account = client.getAccount();

        AccountHome accountHome = homeService.findAccountHomeByAccountId(account.getId());

        S2CHomeDataPacket homeDataPacket = new S2CHomeDataPacket(accountHome);
        connection.sendTCP(homeDataPacket);
    }
}
