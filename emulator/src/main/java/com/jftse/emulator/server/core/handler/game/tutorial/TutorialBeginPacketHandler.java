package com.jftse.emulator.server.core.handler.game.tutorial;

import com.jftse.emulator.server.core.handler.AbstractHandler;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.core.packet.packets.tutorial.C2STutorialBeginRequestPacket;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.networking.packet.Packet;

public class TutorialBeginPacketHandler extends AbstractHandler {
    private C2STutorialBeginRequestPacket tutorialBeginRequestPacket;

    @Override
    public boolean process(Packet packet) {
        tutorialBeginRequestPacket = new C2STutorialBeginRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int tutorialId = tutorialBeginRequestPacket.getTutorialId();

        connection.getClient().setActiveTutorialGame(new TutorialGame(tutorialId));

        Packet answer = new Packet(PacketOperations.C2STutorialBegin.getValueAsChar());
        answer.write((char) 1);
        connection.sendTCP(answer);
    }
}
