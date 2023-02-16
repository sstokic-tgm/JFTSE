package com.jftse.emulator.server.core.packet.packets.tutorial;

import com.jftse.entities.database.model.tutorial.TutorialProgress;
import com.jftse.emulator.server.core.packet.PacketOperations;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CTutorialProgressAnswerPacket extends Packet {
    public S2CTutorialProgressAnswerPacket(List<TutorialProgress> tutorialProgressList) {
        super(PacketOperations.S2CTutorialProgressAck.getValueAsChar());

        this.write((char) tutorialProgressList.size());
        tutorialProgressList.forEach(tpl -> this.write((short) tpl.getTutorial().getTutorialIndex().intValue(), (short) tpl.getSuccess().intValue(), (short) tpl.getAttempts().intValue()));
    }
}
