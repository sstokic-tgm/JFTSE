package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerAnimation;

public class PlayerAnimationTranslator implements IPacketTranslator<SMSGPlayerAnimation, CMSGPlayerAnimation> {
    @Override
    public SMSGPlayerAnimation translate(CMSGPlayerAnimation packet) {
        return SMSGPlayerAnimation.builder()
                .playerPosition(packet.getPlayerPosition())
                .absoluteXPositionOnMap(packet.getAbsoluteXPositionOnMap())
                .absoluteYPositionOnMap(packet.getAbsoluteYPositionOnMap())
                .relativeXMovement(packet.getRelativeXMovement())
                .relativeYMovement(packet.getRelativeYMovement())
                .animationType(packet.getAnimationType())
                .build();
    }
}
