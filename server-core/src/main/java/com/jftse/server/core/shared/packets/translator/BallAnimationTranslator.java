package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGBallAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGBallAnimation;

public class BallAnimationTranslator implements IPacketTranslator<SMSGBallAnimation, CMSGBallAnimation> {
    @Override
    public SMSGBallAnimation translate(CMSGBallAnimation packet) {
        return SMSGBallAnimation.builder()
                .unk0(packet.getUnk0())
                .unk1(packet.getUnk1())
                .absoluteStartXPositionOnMap(packet.getAbsoluteStartXPositionOnMap())
                .absoluteStartYPositionOnMap(packet.getAbsoluteStartYPositionOnMap())
                .absoluteStartZPositionOnMap(packet.getAbsoluteStartZPositionOnMap())
                .absoluteTouchXPositionOnMap(packet.getAbsoluteTouchXPositionOnMap())
                .absoluteTouchYPositionOnMap(packet.getAbsoluteTouchYPositionOnMap())
                .absoluteTouchZPositionOnMap(packet.getAbsoluteTouchZPositionOnMap())
                .speed(packet.getSpeed())
                .hitAct(packet.getHitAct())
                .unk2(packet.getUnk2())
                .powerLevel(packet.getPowerLevel())
                .unk3(packet.getUnk3())
                .playerPosition(packet.getPlayerPosition())
                .build();
    }
}
