package com.jftse.emulator.server.core.handler.tutorial;

import com.jftse.emulator.server.core.singleplay.tutorial.TutorialGame;
import com.jftse.emulator.server.net.FTClient;
import com.jftse.emulator.server.net.FTConnection;
import com.jftse.server.core.handler.PacketHandler;
import com.jftse.server.core.handler.PacketId;
import com.jftse.server.core.shared.packets.SMSGInitSinglePlayGame;
import com.jftse.server.core.shared.packets.tutorial.CMSGRequestTutorialBegin;

@PacketId(CMSGRequestTutorialBegin.PACKET_ID)
public class TutorialBeginPacketHandler implements PacketHandler<FTConnection, CMSGRequestTutorialBegin> {
    @Override
    public void handle(FTConnection connection, CMSGRequestTutorialBegin tutorialBeginRequestPacket) {
        int tutorialId = tutorialBeginRequestPacket.getTutorialId();

        FTClient client = connection.getClient();
        client.setActiveTutorialGame(new TutorialGame(tutorialId));

        SMSGInitSinglePlayGame init = SMSGInitSinglePlayGame.builder()
                .result((char) 1)
                .build();
        connection.sendTCP(init);
    }
}
