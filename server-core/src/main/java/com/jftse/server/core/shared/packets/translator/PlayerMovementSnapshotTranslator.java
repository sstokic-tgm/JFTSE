package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.shared.packets.relay.CMSGPlayerMovementSnapshot;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerMovementSnapshot;

public class PlayerMovementSnapshotTranslator implements IPacketTranslator<SMSGPlayerMovementSnapshot, CMSGPlayerMovementSnapshot> {
    @Override
    public SMSGPlayerMovementSnapshot translate(CMSGPlayerMovementSnapshot packet) {
        return SMSGPlayerMovementSnapshot.builder()
                .playerPosition(packet.getPlayerPosition())
                .ackRttLowByte(packet.getAckRttLowByte())
                .positionX100(packet.getPositionX100())
                .positionZ100(packet.getPositionZ100())
                .velocityX100(packet.getVelocityX100())
                .velocityZ100(packet.getVelocityZ100())
                .action(packet.getAction())
                .build();
    }
}
