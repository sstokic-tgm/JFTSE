package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.core.packets.tutorial.C2STutorialBeginRequestPacket;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.server.core.handler.AbstractPacketHandler;
import com.jftse.server.core.handler.PacketOperationIdentifier;
import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.server.core.protocol.Packet;

@PacketOperationIdentifier(PacketOperations.C2STutorialBegin)
public class TutorialBeginPacketHandler extends AbstractPacketHandler {
    private C2STutorialBeginRequestPacket tutorialBeginRequestPacket;

    @Override
    public boolean process(Packet packet) {
        tutorialBeginRequestPacket = new C2STutorialBeginRequestPacket(packet);
        return true;
    }

    @Override
    public void handle() {
        int tutorialId = tutorialBeginRequestPacket.getTutorialId();

        FTClient client = (FTClient) connection.getClient();
        client.setActiveTutorialGame(new TutorialGame(tutorialId));

        Packet answer = new Packet(PacketOperations.C2STutorialBegin);
        answer.write((char) 1);
        connection.sendTCP(answer);
    }
}
