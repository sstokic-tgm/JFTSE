package com.jftse.emulator.server.core.handler.gacha;

import com.jftse.emulator.server.core.packets.lottery.C2SOpenGachaReqPacket;
import com.jftse.emulator.server.core.packets.lottery.S2COpenGachaAnswerPacket;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.pocket.PlayerPocket;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.LotteryService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SOpenGachaReq)
public class OpenGachaRequestPacketHandler extends AbstractPacketHandler {
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
