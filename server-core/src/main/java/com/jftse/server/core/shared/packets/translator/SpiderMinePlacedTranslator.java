package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGSpiderMinePlaced;
import com.jftse.server.core.shared.packets.relay.SMSGSpiderMinePlaced;

public class SpiderMinePlacedTranslator implements IPacketTranslator<SMSGSpiderMinePlaced, CMSGSpiderMinePlaced> {
    @Override
    public SMSGSpiderMinePlaced translate(CMSGSpiderMinePlaced packet) {
        return SMSGSpiderMinePlaced.builder()
                .position(packet.getPosition())
                .isActive(packet.getIsActive())
                .mineId(packet.getMineId())
                .posX(packet.getPosX())
                .posY(packet.getPosY())
                .build();
    }
}
