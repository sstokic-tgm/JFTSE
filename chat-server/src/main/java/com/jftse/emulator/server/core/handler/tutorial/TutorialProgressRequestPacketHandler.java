package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.TutorialService;

import java.util.List;

@PacketOperationIdentifier(PacketOperations.C2STutorialProgressReq)
public class TutorialProgressRequestPacketHandler extends AbstractPacketHandler {
    private final TutorialService tutorialService;

    public TutorialProgressRequestPacketHandler() {
        tutorialService = ServiceManager.getInstance().getTutorialService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        List<TutorialProgress> tutorialProgressList = tutorialService.findAllByPlayerIdFetched(client.getPlayer().getId());

        S2CTutorialProgressAnswerPacket tutorialProgressAnswerPacket = new S2CTutorialProgressAnswerPacket(tutorialProgressList);
        connection.sendTCP(tutorialProgressAnswerPacket);
    }
}
