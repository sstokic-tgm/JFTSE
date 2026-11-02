package com.jftse.server.core.shared.packets.translator;

import com.jftse.server.core.protocol.IPacketTranslator;
import com.jftse.server.core.protocol.Packet;
import com.jftse.server.core.shared.packets.relay.SMSGPlayerMovementState;

public class PlayerMovementStateTranslator implements IPacketTranslator<SMSGPlayerMovementState, Packet> {
    public static final int PAYLOAD_LENGTH = 18;

    @Override
    public SMSGPlayerMovementState translate(Packet packet) {
        if (packet.getPacketId() != SMSGPlayerMovementState.PACKET_ID || packet.getDataLength() != PAYLOAD_LENGTH)
            throw new IllegalArgumentException("Expected a movement-state packet");

        Packet movement = new Packet(packet);
        movement.setReadPosition(0);
        return SMSGPlayerMovementState.builder()
                .playerPosition(movement.read(Byte.class))
                .movementState(movement.read(Byte.class))
                .movementSpeed(movement.read(Byte.class))
                .directionX100(movement.read(Short.class))
                .directionZ100(movement.read(Short.class))
                .positionX100(movement.read(Short.class))
                .positionZ100(movement.read(Short.class))
                .knockbackSpeed(movement.read(Byte.class))
                .approachTargetX100(movement.read(Short.class))
                .approachTargetZ100(movement.read(Short.class))
                .followThrough(movement.read(Byte.class))
                .followThroughDuration100(movement.read(Byte.class))
                .build();
    }
}
