package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGSpiderMineExplode;
import com.jftse.server.core.shared.packets.relay.SMSGSpiderMineExplode;

public class SpiderMineExplodeTranslator implements IPacketTranslator<SMSGSpiderMineExplode, CMSGSpiderMineExplode> {
    @Override
    public SMSGSpiderMineExplode translate(CMSGSpiderMineExplode packet) {
        return SMSGSpiderMineExplode.builder()
                .targetPosition(packet.getTargetPosition())
                .spiderMineId(packet.getSpiderMineId())
                .build();
    }
}
