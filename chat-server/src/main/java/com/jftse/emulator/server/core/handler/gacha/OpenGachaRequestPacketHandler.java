package com.jftse.emulator.server.core.handler.gacha;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.LotteryService;
import com.jftse.server.core.shared.packets.gacha.CMSGOpenGacha;

import java.util.List;

@PacketId(CMSGOpenGacha.PACKET_ID)
public class OpenGachaRequestPacketHandler implements PacketHandler<FTConnection, CMSGOpenGacha> {
    private final LotteryService lotteryService;

    public OpenGachaRequestPacketHandler() {
        lotteryService = ServiceManager.getInstance().getLotteryService();
    }

    @Override
    public void handle(FTConnection connection, CMSGOpenGacha openGachaReqPacket) {
        long playerPocketId = openGachaReqPacket.getPlayerPocketId();
        int productIndex = openGachaReqPacket.getProductIndex();

        List<PlayerPocket> playerPocketList = lotteryService.drawLottery(connection, playerPocketId, productIndex);

        S2COpenGachaAnswerPacket openGachaAnswerPacket = new S2COpenGachaAnswerPacket(playerPocketList);
        connection.sendTCP(openGachaAnswerPacket);
    }
}
