package com.jftse.emulator.server.core.handler.challenge;

import com.jftse.emulator.server.core.packets.challenge.S2CChallengeProgressAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.challenge.ChallengeProgress;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.ChallengeService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2SChallengeProgressReq)
public class ChallengeProgressRequestPacketHandler extends AbstractPacketHandler {
    private final ChallengeService challengeService;

    public ChallengeProgressRequestPacketHandler() {
        challengeService = ServiceManager.getInstance().getChallengeService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        List<ChallengeProgress> challengeProgressList = challengeService.findAllByPlayerIdFetched(client.getPlayer().getId());

        S2CChallengeProgressAnswerPacket challengeProgressAnswerPacket = new S2CChallengeProgressAnswerPacket(challengeProgressList);
        connection.sendTCP(challengeProgressAnswerPacket);
    }
}
