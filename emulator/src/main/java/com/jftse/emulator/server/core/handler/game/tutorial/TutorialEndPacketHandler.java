package com.jftse.emulator.server.core.handler.game.tutorial;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.service.TutorialService;
import com.jftse.emulator.server.networking.packet.Packet;

public class TutorialEndPacketHandler extends AbstractHandler {
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
        connection.getClient().getActiveTutorialGame().finishTutorial();
        tutorialService.finishGame(connection);

        connection.getClient().setActiveTutorialGame(null);
    }
}
