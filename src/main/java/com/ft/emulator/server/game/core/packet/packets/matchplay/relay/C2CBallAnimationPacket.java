package com.ft.emulator.server.game.core.packet.packets.matchplay.relay;

import com.ft.emulator.server.networking.packet.Packet;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class C2CBallAnimationPacket extends Packet {
    private short absoluteStartXPositionOnMap;
    private short absoluteStartYPositionOnMap;
    private short absoluteTouchXPositionOnMap;
    private short absoluteTouchYPositionOnMap;
    private byte ballSpeed;
    private byte ballAbility;

    public C2CBallAnimationPacket(Packet packet) {
        super(packet);

        this.setReadPosition(this.getReadPosition() + 13); // Unknown bytes
        this.absoluteStartXPositionOnMap = this.readShort();
        this.absoluteStartYPositionOnMap = this.readShort();
        this.readShort(); // Unknown
        this.absoluteTouchXPositionOnMap = this.readShort();
        this.absoluteTouchYPositionOnMap = this.readShort();
        this.readShort(); // Unknown
        this.readByte(); // Unknown
        this.ballSpeed = this.readByte();
        this.readByte(); // Unknown
        this.readByte(); // Unknown
        this.ballAbility = this.readByte();
    }
}
