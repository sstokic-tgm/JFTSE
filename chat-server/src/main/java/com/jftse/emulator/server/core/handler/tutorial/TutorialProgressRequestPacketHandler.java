package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.TutorialService;
import com.jftse.server.core.shared.packets.tutorial.CMSGTutorialProgress;

import java.util.List;

@PacketId(CMSGTutorialProgress.PACKET_ID)
public class TutorialProgressRequestPacketHandler implements PacketHandler<FTConnection, CMSGTutorialProgress> {
    private final TutorialService tutorialService;

    public TutorialProgressRequestPacketHandler() {
        tutorialService = ServiceManager.getInstance().getTutorialService();
    }

    @Override
    public void handle(FTConnection connection, CMSGTutorialProgress packet) {
        FTClient client = connection.getClient();
        List<TutorialProgress> tutorialProgressList = tutorialService.findAllByPlayerIdFetched(client.getPlayer().getId());

        S2CTutorialProgressAnswerPacket tutorialProgressAnswerPacket = new S2CTutorialProgressAnswerPacket(tutorialProgressList);
        connection.sendTCP(tutorialProgressAnswerPacket);
    }
}
