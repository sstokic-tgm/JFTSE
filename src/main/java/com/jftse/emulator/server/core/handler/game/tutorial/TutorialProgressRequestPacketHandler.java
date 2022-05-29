package com.jftse.emulator.server.core.handler.game.tutorial;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.manager.ServiceManager;
import com.jftse.emulator.server.core.packet.packets.tutorial.S2CTutorialProgressAnswerPacket;
import com.jftse.emulator.server.core.service.TutorialService;
import com.jftse.emulator.server.database.model.tutorial.TutorialProgress;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class TutorialProgressRequestPacketHandler extends AbstractHandler {
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
        List<TutorialProgress> tutorialProgressList = tutorialService.findAllByPlayerIdFetched(connection.getClient().getPlayer().getId());

        S2CTutorialProgressAnswerPacket tutorialProgressAnswerPacket = new S2CTutorialProgressAnswerPacket(tutorialProgressList);
        connection.sendTCP(tutorialProgressAnswerPacket);
    }
}
