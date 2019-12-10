package com.ft.emulator.server.game.server.packets.tutorial;

import com.ft.emulator.server.database.model.tutorial.TutorialProgress;
import com.ft.emulator.server.game.server.Packet;
import com.ft.emulator.server.game.server.PacketID;

import java.util.List;

public class S2CTutorialProgressAnswerPacket extends Packet {

    public S2CTutorialProgressAnswerPacket(List<TutorialProgress> tutorialProgressList) {

        super(PacketID.S2CTutorialProgressAck);

        this.write((char)tutorialProgressList.size());
        for(TutorialProgress tutorialProgress : tutorialProgressList) {

            this.write((short)Math.toIntExact(tutorialProgress.getTutorial().getTutorialIndex()));
            this.write((short)tutorialProgress.getSuccess().intValue());
            this.write((short)tutorialProgress.getAttempts().intValue());
	}
    }
}