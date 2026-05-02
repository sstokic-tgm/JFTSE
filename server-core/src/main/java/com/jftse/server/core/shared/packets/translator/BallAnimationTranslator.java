package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGBallAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGBallAnimation;

public class BallAnimationTranslator implements IPacketTranslator<SMSGBallAnimation, CMSGBallAnimation> {
    @Override
    public SMSGBallAnimation translate(CMSGBallAnimation packet) {
        return SMSGBallAnimation.builder()
                .progress(packet.getProgress())
                .state(packet.getState())
                .originX(packet.getOriginX())
                .originY(packet.getOriginY())
                .originZ(packet.getOriginZ())
                .pathX(packet.getPathX())
                .pathZ(packet.getPathZ())
                .curveControl(packet.getCurveControl())
                .speed(packet.getSpeed())
                .hitAct(packet.getHitAct())
                .shotCode(packet.getShotCode())
                .powerLevel(packet.getPowerLevel())
                .specialShotId(packet.getSpecialShotId())
                .playerPosition(packet.getPlayerPosition())
                .build();
    }
}
