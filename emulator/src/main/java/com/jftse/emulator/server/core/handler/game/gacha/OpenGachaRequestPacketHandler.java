package com.jftse.emulator.server.core.handler.game.gacha;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.lottery.C2SOpenGachaReqPacket;
import com.jftse.emulator.server.core.packet.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.emulator.server.core.service.LotteryService;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class OpenGachaRequestPacketHandler extends AbstractHandler {
    private C2SOpenGachaReqPacket openGachaReqPacket;

    private final LotteryService lotteryService;

    public OpenGachaRequestPacketHandler() {
        lotteryService = ServiceManager.getInstance().getLotteryService();
    }

    @Override
    public boolean process(Packet packet) {
        openGachaReqPacket = new C2SOpenGachaReqPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        long playerPocketId = openGachaReqPacket.getPlayerPocketId();
        int productIndex = openGachaReqPacket.getProductIndex();

        List<PlayerPocket> playerPocketList = lotteryService.drawLottery(connection, playerPocketId, productIndex);

        S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(playerPocketList);
        connection.sendTCP(openGachaAnswerPacket);
    }
}
