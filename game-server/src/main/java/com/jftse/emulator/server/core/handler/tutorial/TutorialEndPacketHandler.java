package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.service.TutorialService;

@PacketOperationIdentifier(PacketOperations.C2STutorialEnd)
public class TutorialEndPacketHandler extends AbstractPacketHandler {
    private final TutorialService tutorialService;

    public TutorialEndPacketHandler() {
        tutorialService = ServiceManager.getInstance().getTutorialService();
    }

    @Override
    public boolean process(Packet packet) {
        return true;
    }

    @Override
    public void handle() {
        FTClient client = (FTClient) connection.getClient();
        client.getActiveTutorialGame().finishTutorial();
        tutorialService.finishGame(connection);

        client.setActiveTutorialGame(null);
    }
}
