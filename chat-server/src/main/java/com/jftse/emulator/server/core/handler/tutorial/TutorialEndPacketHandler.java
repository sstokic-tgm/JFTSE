package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.service.TutorialService;
import com.jftse.server.core.shared.packets.tutorial.CMSGTutorialEnd;

@PacketId(CMSGTutorialEnd.PACKET_ID)
public class TutorialEndPacketHandler implements PacketHandler<FTConnection, CMSGTutorialEnd> {
    private final TutorialService tutorialService;

    public TutorialEndPacketHandler() {
        tutorialService = ServiceManager.getInstance().getTutorialService();
    }

    @Override
    public void handle(FTConnection connection, CMSGTutorialEnd packet) {
        FTClient client = connection.getClient();
        client.getActiveTutorialGame().finishTutorial();
        tutorialService.finishGame(connection);

        client.setActiveTutorialGame(null);
    }
}
