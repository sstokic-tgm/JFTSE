package com.jftse.emulator.server.game.core.packet.packets.tutorial;

import com.jftse.emulator.server.database.model.tutorial.TutorialProgress;
import com.jftse.emulator.server.game.core.packet.PacketID;
import com.jftse.emulator.server.networking.packet.Packet;

import java.util.List;

public class S2CTutorialProgressAnswerPacket extends Packet {
    public S2CTutorialProgressAnswerPacket(List<TutorialProgress> tutorialProgressList) {
        super(PacketID.S2CTutorialProgressAck);

        this.write((char) tutorialProgressList.size());
        tutorialProgressList.forEach(tpl -> this.write((short) tpl.getTutorial().getTutorialIndex().intValue(), (short) tpl.getSuccess().intValue(), (short) tpl.getAttempts().intValue()));
    }
}
