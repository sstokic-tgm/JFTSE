package com.jftse.emulator.server.core.packets.tutorial;

import com.jftse.server.core.protocol.PacketOperations;
import com.jftse.server.core.protocol.Packet;
import com.jftse.entities.database.model.tutorial.TutorialProgress;

import java.util.List;

public class S2CTutorialProgressAnswerPacket extends Packet {
    public S2CTutorialProgressAnswerPacket(List<TutorialProgress> tutorialProgressList) {
        super(PacketOperations.S2CTutorialProgressAck.getValue());

        this.write((char) tutorialProgressList.size());
        tutorialProgressList.forEach(tpl -> this.write((short) tpl.getTutorial().getTutorialIndex().intValue(), (short) tpl.getSuccess().intValue(), (short) tpl.getAttempts().intValue()));
    }
}
