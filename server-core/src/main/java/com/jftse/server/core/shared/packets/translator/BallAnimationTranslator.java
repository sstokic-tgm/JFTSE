package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGBallAnimation;
import com.jftse.server.core.shared.packets.relay.SMSGBallAnimation;

public class BallAnimationTranslator implements IPacketTranslator<SMSGBallAnimation, CMSGBallAnimation> {
    @Override
    public SMSGBallAnimation translate(CMSGBallAnimation packet) {
        return SMSGBallAnimation.builder()
                .unk0(packet.getUnk0())
                .absoluteStartXPositionOnMap(packet.getAbsoluteStartXPositionOnMap())
                .absoluteStartYPositionOnMap(packet.getAbsoluteStartYPositionOnMap())
                .absoluteStartZPositionOnMap(packet.getAbsoluteStartZPositionOnMap())
                .absoluteTouchXPositionOnMap(packet.getAbsoluteTouchXPositionOnMap())
                .absoluteTouchYPositionOnMap(packet.getAbsoluteTouchYPositionOnMap())
                .absoluteTouchZPositionOnMap(packet.getAbsoluteTouchZPositionOnMap())
                .unk1(packet.getUnk1())
                .ballSpeed(packet.getBallSpeed())
                .ballAnimation(packet.getBallAnimation())
                .unk2(packet.getUnk2())
                .ballAbility(packet.getBallAbility())
                .unk3(packet.getUnk3())
                .playerPosition(packet.getPlayerPosition())
                .build();
    }
}
