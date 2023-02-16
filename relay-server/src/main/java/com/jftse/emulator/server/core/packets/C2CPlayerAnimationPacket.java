package com.jftse.emulator.server.core.packets;

import com.jftse.server.core.protocol.Packet;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class C2CPlayerAnimationPacket extends Packet {
    private char playerPosition;
    private short absoluteXPositionOnMap;
    private short absoluteYPositionOnMap;
    private short relativeXMovement;
    private short relativeYMovement;
    private byte animationType;

    public C2CPlayerAnimationPacket(Packet packet) {
        super(packet);

        this.playerPosition = this.readChar();
        this.absoluteXPositionOnMap = this.readShort();
        this.absoluteYPositionOnMap = this.readShort();
        this.relativeXMovement = this.readShort();
        this.relativeYMovement = this.readShort();
        this.animationType = this.readByte();
    }
}
